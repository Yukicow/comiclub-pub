package com.comiclub.domain.service.work;


import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.comiclub.domain.entity.board.EpisodeBoard;
import com.comiclub.domain.entity.sereis.*;
import com.comiclub.domain.repository.board.episode.EBoardRepository;
import com.comiclub.domain.repository.series.*;
import com.comiclub.web.contoller.mywork.dto.BgSoundInfo;
import com.comiclub.web.contoller.mywork.dto.LayerDto;
import com.comiclub.web.contoller.mywork.dto.SeriesInfo;
import com.comiclub.web.contoller.mywork.form.SeriesForm;
import com.comiclub.web.contoller.mywork.form.WorkForm;
import com.comiclub.web.exception.NoAuthorizationException;
import com.comiclub.web.exception.NotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.amazonaws.services.s3.model.DeleteObjectsRequest.*;
import static com.comiclub.domain.util.constant.WorkConst.*;

@Service
@Transactional
@RequiredArgsConstructor
public class MySeriesService {

    private final SeriesRepository seriesRepository;
    private final EpisodeRepository episodeRepository;
    private final EpBgSoundRepository bgSoundRepository;
    private final EpSceneRepository sceneRepository;
    private final EpLayerRepository layerRepository;
    private final EBoardRepository eBoardRepository;


    // s3
    private final AmazonS3Client amazonS3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;



    private final String s3Url = "https://comiclub-s3.s3.ap-northeast-2.amazonaws.com/";
    private String getSeriesPath(String storeFileName) {
        return "series/" + storeFileName;
    }
    private String getSeriesUrl(String storeFileName) {
        return s3Url + "series/" + storeFileName;
    }
    private String extractKey(String url) {
        return url.substring(s3Url.length());
    }
    private String getFileName(String url) {
        int pos = url.lastIndexOf("/");
        return url.substring(pos + 1);
    }

    public String createFileName(String originFileName) {
        int pos = originFileName.lastIndexOf(".");
        String ext = originFileName.substring(pos);
        return UUID.randomUUID() + ext;
    }




    @Transactional(readOnly = true)
    public Slice<SeriesInfo> searchMySeries(Long seriesId, String name, Pageable pageable) {
        Slice<Series> seriesSlice = seriesRepository.searchMySeries(seriesId, name, pageable);
        List<SeriesInfo> series = seriesSlice.stream()
                .map(s -> new SeriesInfo(s))
                .collect(Collectors.toList());

        return new SliceImpl<>(series, seriesSlice.getPageable(), seriesSlice.hasNext());
    }

    @Transactional(rollbackFor = IOException.class)
    public Long saveSeries(Long memberId, String writer, SeriesForm form, MultipartFile multipartFile) throws IOException {

        Series series = Series.createNewSeries(
                memberId,
                form.getAdultOnly(),
                form.getName(),
                form.getDescription(),
                writer,
                null,
                form.getPub(),
                form.getTag()
        );

        Series savedSeries = seriesRepository.save(series);
        if(multipartFile != null && !multipartFile.isEmpty()){
            String storeFileName = createFileName(multipartFile.getOriginalFilename());
            String filePath = getSeriesPath(storeFileName);

            uploadFileToS3(multipartFile, filePath);

            savedSeries.changeThumbnailUrl(getSeriesUrl(storeFileName));

            /**
             * 혹시나 DB에서 동작에 문제가 생길 경우 저장된 파일을 지워야함. (처리해 주지 않으면 영구 저장됨 )
             * */
            try {
                seriesRepository.flush();
            }catch (Exception e){
                amazonS3Client.deleteObject(bucket, extractKey(series.getThumbnailUrl()));
                throw e;
            }
        }

        return series.getId();
    }
    public void deleteSeries(Long seriesId, Long memberId) {

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new NotFoundException("There is no series id '" + seriesId + "'"));

        if (!series.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for removing series");

        ArrayList<KeyVersion> keys = new ArrayList<>();

        // 시리즈 썸네일 추가
        if(StringUtils.hasText(series.getThumbnailUrl()))
            keys.add(new KeyVersion(extractKey(series.getThumbnailUrl())));

        episodeRepository.findBySeriesId(seriesId).forEach(episode -> {
            if(StringUtils.hasText(episode.getThumbnailUrl()))
                keys.add(new KeyVersion(extractKey(episode.getThumbnailUrl())));

            // 배경음악 파일 추가 
            episode.getBgSounds().stream()
                    .filter(bs -> StringUtils.hasText(bs.getFileUrl()))
                    .forEach(bs -> keys.add(new KeyVersion(extractKey(bs.getFileUrl()))));

            // 장면 파일 추가
            // 각각의 장면에 해당하는 모든 파일 추가 (레이어 이미지 및 장면 효과음)
            episode.getScenes().forEach(scene -> {
                if (StringUtils.hasText(scene.getAudioFileUrl()))
                    keys.add(new KeyVersion(extractKey(scene.getAudioFileUrl())));

                scene.getLayers().stream()
                        .filter(layer -> StringUtils.hasText(layer.getImgFileUrl()))
                        .forEach(layer -> keys.add(new KeyVersion(extractKey(layer.getImgFileUrl()))));
            });
        });

        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket)
                .withKeys(keys)
                .withQuiet(false);

        if(keys.size() > 0) amazonS3Client.deleteObjects(deleteObjectsRequest);

        seriesRepository.delete(series);
    }

    @Transactional(rollbackFor = IOException.class)
    public void updateSeries(Long seriesId, Long memberId, SeriesForm form, MultipartFile multipartFile) throws IOException {

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new NotFoundException("There is no series id '" + seriesId + "'"));

        if (!series.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for updating series");


        series.changeSeriesName(form.getName());
        series.changeDescription(form.getDescription());
        series.changeTag(form.getTag());
        series.changeAdultOnly(form.getAdultOnly());
        series.changePub(form.getPub());

        if (multipartFile != null && !multipartFile.isEmpty()) {
            String fileName = series.getThumbnailUrl() != null
                    ? getFileName(series.getThumbnailUrl())
                    : createFileName(multipartFile.getOriginalFilename());
            String filePath = getSeriesPath(fileName);

            uploadFileToS3(multipartFile, filePath);

            series.changeThumbnailUrl(getSeriesUrl(fileName));

            /**
             * 혹시나 DB에서 동작에 문제가 생길 경우 저장된 파일을 지워야함. (처리해 주지 않으면 영구 저장됨 )
             * */
            try {
                seriesRepository.flush();
            }catch (Exception e){
                amazonS3Client.deleteObject(bucket, extractKey(series.getThumbnailUrl()));
                throw e;
            }

        }
    }



    /**
     *  Episode관련
     */
    @Transactional(rollbackFor = IOException.class)
    public Long saveEpisode(Long seriesId, Long memberId, WorkForm form, MultipartFile multipartFile) throws IOException {

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new NotFoundException("There is no series id '" + seriesId + "'"));

        if (!series.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for creating episode");

        Episode episode = Episode.createNewEpisode(
                memberId,
                seriesId,
                form.getTitle(),
                series.getTotalEp() + 1,
                null,
                form.getAutoMode(),
                form.getFreeUse()
        );

        episodeRepository.save(episode);
        seriesRepository.increaseTotalEp(episode.getSeriesId());

        if(multipartFile != null && !multipartFile.isEmpty()){
            String fileName = createFileName(multipartFile.getOriginalFilename());
            String filePath = getSeriesPath(fileName);

            uploadFileToS3(multipartFile, filePath);

            episode.changeThumbnailUrl(getSeriesUrl(fileName));

            /**
             * 혹시나 DB에서 동작에 문제가 생길 경우 저장된 파일을 지워야함. (처리해 주지 않으면 영구 저장됨 )
             * */
            try {
                episodeRepository.flush();
            }catch (Exception e){
                amazonS3Client.deleteObject(bucket, extractKey(episode.getThumbnailUrl()));
                throw e;
            }

        }

        return episode.getId();
    }

    @Transactional(rollbackFor = IOException.class)
    public void updateEpisode(Long episodeId, Long memberId, WorkForm form, MultipartFile multipartFile) throws IOException {

        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new NotFoundException("There is no episode id '" + episodeId + "'"));

        if (!episode.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for updating episode");

        episode.changeTitle(form.getTitle());
        episode.changeAutoMode(form.getAutoMode());
        episode.changeFreeUse(form.getFreeUse());

        if(multipartFile != null && !multipartFile.isEmpty()){
            String fileName = episode.getThumbnailUrl() != null
                    ? getFileName(episode.getThumbnailUrl())
                    : createFileName(multipartFile.getOriginalFilename());
            String filePath = getSeriesPath(fileName);

            uploadFileToS3(multipartFile, filePath);

            episode.changeThumbnailUrl(getSeriesUrl(fileName));

            EpisodeBoard board = eBoardRepository.findOneByEpisodeId(episode.getId()).orElse(null);
            if(board != null) board.changeThumbnailUrl(episode.getThumbnailUrl());


            /**
             * 혹시나 DB에서 동작에 문제가 생길 경우 저장된 파일을 지워야함. (처리해 주지 않으면 영구 저장됨 )
             * */
            try {
                episodeRepository.flush();
            }catch (Exception e){
                amazonS3Client.deleteObject(bucket, extractKey(episode.getThumbnailUrl()));
                throw e;
            }

        }
    }

    public Long deleteEpisode(Long episodeId, Long memberId) {

        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new NotFoundException("There is no episode id '" + episodeId + "'"));

        if (!episode.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for removing episode");

        // 삭제될 모든 파일에 대한 정보를 담음
        ArrayList<KeyVersion> keys = new ArrayList<>();

        // 작품에 대한 모든 파일 추가 (배경음악 및 썸네일)
        if(StringUtils.hasText(episode.getThumbnailUrl()))
            keys.add(new KeyVersion(extractKey(episode.getThumbnailUrl())));

        episode.getBgSounds().stream()
                .filter(bs -> StringUtils.hasText(bs.getFileUrl()))
                .forEach(bs -> keys.add(new KeyVersion(extractKey(bs.getFileUrl()))));

        // 각각의 장면에 해당하는 모든 파일 추가 (레이어 이미지 및 장면 효과음)
        episode.getScenes().forEach(scene -> {
            if(StringUtils.hasText(scene.getAudioFileUrl()))
                keys.add(new KeyVersion(extractKey(scene.getAudioFileUrl())));

            scene.getLayers().stream()
                    .filter(layer -> StringUtils.hasText(layer.getImgFileUrl()))
                    .forEach(layer -> keys.add(new KeyVersion(extractKey(layer.getImgFileUrl()))));
        });

        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket)
                .withKeys(keys)
                .withQuiet(false);

        // 모든 파일을 S3에서 삭제
        if(keys.size() > 0) amazonS3Client.deleteObjects(deleteObjectsRequest);

        // 에피소드 삭제 및 시리즈의 총 에피소드 수 감소
        Long seriesId = episode.getSeriesId();
        episodeRepository.delete(episode);
        seriesRepository.decreaseTotalEp(seriesId);

        // 에피소드에 대한 게시물이 존재하면 총 게시물 수도 감소
        boolean boardExist = eBoardRepository.existsByEpisodeId(episodeId);
        if(boardExist) 
            seriesRepository.decreaseTotalEpBoard(seriesId);

        return seriesId;
    }

    @Transactional(rollbackFor = IOException.class)
    public BgSoundInfo saveEpisodeBgSound(Long episodeId, Long memberId, MultipartFile multipartFile) throws IOException {

        Episode episode = episodeRepository.findWithBgSoundsById(episodeId)
                .orElseThrow(() -> new NotFoundException("There is no episode id '" + episodeId + "'"));

        if (!episode.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for creating sound");

        int nextSoundNum = episode.getBgSounds().size() + 1;
        if(nextSoundNum >= MAX_BG_SOUND_SIZE)
            throw new ValidationException("The next BackgroundSound number grater than or equal to max size");

        String storeFileName = createFileName(multipartFile.getOriginalFilename());
        EpBackgroundSound bgSound = EpBackgroundSound.createNewEpBackgroundSound(
                episode,
                memberId,
                storeFileName,
                null,
                1,
                2
        );

        String filePath = getSeriesPath(storeFileName);
        uploadFileToS3(multipartFile, filePath);

        bgSound.changeFileUrl(getSeriesUrl(storeFileName));

        bgSoundRepository.save(bgSound);

        /**
         * 혹시나 DB에서 동작에 문제가 생길 경우 저장된 파일을 지워야함. (처리해 주지 않으면 영구 저장됨 )
         * */
        try {
            bgSoundRepository.flush();
        }catch (Exception e){
            amazonS3Client.deleteObject(bucket, extractKey(bgSound.getFileUrl()));
            throw e;
        }

        return new BgSoundInfo(bgSound);
    }

    public void deleteEpisodeBgSound(Long soundId, Long memberId) {

        EpBackgroundSound bgSound = bgSoundRepository.findById(soundId)
                .orElseThrow(() -> new NotFoundException("There is no BackgroundSound"));

        if (!bgSound.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for removing sound");

        bgSoundRepository.delete(bgSound);
        if(StringUtils.hasText(bgSound.getFileUrl()))
            amazonS3Client.deleteObject(bucket, extractKey(bgSound.getFileUrl()));
    }

    public void updateEpisodeBgSound(Long soundId, Long memberId, int startNum, int endNum) {

        EpBackgroundSound bgSound = bgSoundRepository.findById(soundId)
                .orElseThrow(() -> new NotFoundException("There is no BackgroundSound"));

        if (!bgSound.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for updating sound");

        bgSound.changeStartSceneNumber(startNum);
        bgSound.changeEndSceneNumber(endNum);
    }

    public Long saveEpisodeScene(Long episodeId, Long memberId) {

        Episode episode = episodeRepository.findWithScenesById(episodeId)
                .orElseThrow(() -> new NotFoundException("There is no episode id '" + episodeId + "'"));

        if (!episode.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for creating scene");

        int nextSceneNum = episode.getScenes().size() + 1;
        if (nextSceneNum > MAX_SCENE_SIZE)
            throw new ValidationException("The next number of scene over max size");

        EpisodeScene scene = EpisodeScene.createNewEpisodeScene(episode, memberId, nextSceneNum, 1, null, null, 0.0);
        EpisodeLayer layer = EpisodeLayer.createNewEpisodeLayer(scene, memberId,1, 0.0, null);

        sceneRepository.save(scene);
        layerRepository.save(layer);

        return scene.getId();
    }

    public Long saveEpisodeSceneToFront(Long episodeId, Long memberId, int sceneNumber) {

        Episode episode = episodeRepository.findWithScenesById(episodeId)
                .orElseThrow(() -> new NotFoundException("There is no episode id '" + episodeId + "'"));

        if (!episode.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for creating scene");

        int totalScene = episode.getScenes().size();
        if (totalScene >= MAX_SCENE_SIZE)
            throw new ValidationException("Current total size of scene equal to max size");

        EpisodeScene scene = EpisodeScene.createNewEpisodeScene(episode, memberId, sceneNumber, 1, null, null, 0.0);
        EpisodeLayer layer = EpisodeLayer.createNewEpisodeLayer(scene, memberId,1, 0.0, null);

        sceneRepository.increaseSceneNumbers(episodeId, scene.getSceneNumber());

        sceneRepository.save(scene);
        layerRepository.save(layer);

        return scene.getId();
    }

    public void deleteEpisodeScene(Long sceneId, Long memberId) {

        EpisodeScene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new NotFoundException("There is no scene id '" + sceneId + "'"));

        if(!scene.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for removing scene");

        // 삭제될 모든 파일에 대한 정보를 담음
        ArrayList<KeyVersion> keys = new ArrayList<>();

        // 각각의 장면에 해당하는 모든 파일 추가 (레이어 이미지 및 장면 효과음)
        if(StringUtils.hasText(scene.getAudioFileUrl()))
            keys.add(new KeyVersion(extractKey(scene.getAudioFileUrl())));

        scene.getLayers().stream()
                .filter(layer -> StringUtils.hasText(layer.getImgFileUrl()))
                .forEach(layer -> keys.add(new KeyVersion(extractKey(layer.getImgFileUrl()))));

        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket)
                .withKeys(keys)
                .withQuiet(false);

        if(keys.size() > 0) amazonS3Client.deleteObjects(deleteObjectsRequest);

        sceneRepository.delete(scene);
        sceneRepository.decreaseSceneNumbers(scene.getEpisode().getId(), scene.getSceneNumber());
    }

    @Transactional(rollbackFor = IOException.class)
    public void updateEpisodeSceneAudio(Long sceneId, Long memberId, MultipartFile multipartFile) throws IOException {

        EpisodeScene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new NotFoundException("There is no scene id '" + sceneId + "'"));

        if (!scene.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for updating scene");

        if(multipartFile != null && !multipartFile.isEmpty()){

            String fileName = scene.getAudioFileUrl() != null
                    ? getFileName(scene.getAudioFileUrl())
                    : createFileName(multipartFile.getOriginalFilename());

            String filePath = getSeriesPath(fileName);

            uploadFileToS3(multipartFile, filePath);

            scene.changeAudioFileUrl(getSeriesUrl(fileName));

            /**
             * 혹시나 DB에서 동작에 문제가 생길 경우 저장된 파일을 지워야함. (처리해 주지 않으면 영구 저장됨 )
             * */
            try {
                sceneRepository.flush();
            }catch (Exception e){
                amazonS3Client.deleteObject(bucket, extractKey(scene.getAudioFileUrl()));
                throw e;
            }
        }
        else{

            if(scene.getAudioFileUrl() != null){
                amazonS3Client.deleteObject(bucket, extractKey(scene.getAudioFileUrl()));
            }

            scene.changeAudioFileUrl(null);
        }
    }

    public LayerDto saveEpisodeLayer(Long sceneId, Long memberId){

        EpisodeScene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new NotFoundException("There is no scene id '" + sceneId + "'"));

        if (!scene.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for creating layer");

        Integer nextLayerNumber = scene.getTotalLayer() + 1;
        if(nextLayerNumber > MAX_LAYER_SIZE)
            throw new ValidationException("Layer number should be less than 4");

        EpisodeLayer layer = EpisodeLayer.createNewEpisodeLayer(
                scene,
                memberId,
                nextLayerNumber,
                0.0,
                null
        );

        layer.save();
        layerRepository.save(layer);

        return new LayerDto(layer);
    }

    public void updateEpisodeLayerImage(Long layerId, Long memberId, MultipartFile multipartFile) throws IOException {

        EpisodeLayer layer = layerRepository.findById(layerId)
                .orElseThrow(() -> new NotFoundException("There is no Layer id '" + layerId + "'"));

        if (!layer.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for creating layer");

        String fileName = layer.getImgFileUrl() != null
                ? getFileName(layer.getImgFileUrl())
                : createFileName(multipartFile.getOriginalFilename());
        String filePath = getSeriesPath(fileName);

        uploadFileToS3(multipartFile, filePath);

        layer.changeImageFileUrl(getSeriesUrl(fileName));

        if(layer.getLayerNumber().equals(FIRST_LAYER)){
            EpisodeScene scene = sceneRepository.findById(layer.getScene().getId())
                    .orElseThrow(() -> new NotFoundException("There is no scene id '" + layer.getScene().getId() + "'"));
            scene.changeFirstLayerImgFileUrl(layer.getImgFileUrl());
        }

        /**
         * 혹시나 DB에서 동작에 문제가 생길 경우 저장된 파일을 지워야함. (처리해 주지 않으면 영구 저장됨 )
         * */
        try {
            layerRepository.flush();
        }catch (Exception e){
            amazonS3Client.deleteObject(bucket, extractKey(layer.getImgFileUrl()));
            throw e;
        }
    }

    public void deleteEpisodeLayer(Long layerId, Long memberId) {

        EpisodeLayer layer = layerRepository.findById(layerId)
                .orElseThrow(() -> new NotFoundException("There is no layer id'" + layerId + "'"));

        if (!layer.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for removing layer");

        /**
         * layer의 유니크 제약 조건때문에 미리 Database에서 지우는 작업이 필요
         * */
        layerRepository.delete(layer);
        layerRepository.flush();

        layer.delete();

        if(layer.getImgFileUrl() != null){
            amazonS3Client.deleteObject(bucket, extractKey(layer.getImgFileUrl()));
        }
    }


    public void updateDurations(Long sceneId, Long memberId, Map<Long, Double> durationMap) {

        EpisodeScene scene = sceneRepository.findWithLayersById(sceneId)
                .orElseThrow(() -> new NotFoundException("There is no scene id '" + sceneId + "'"));

        if (!scene.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for updating layer");

        scene.updateDuration(durationMap);
    }

    private void uploadFileToS3(MultipartFile multipartFile, String filePath) throws IOException {
        ObjectMetadata metadata= new ObjectMetadata();
        metadata.setContentType(multipartFile.getContentType());
        metadata.setContentLength(multipartFile.getSize());
        amazonS3Client.putObject(bucket, filePath, multipartFile.getInputStream(), metadata);
    }
}
