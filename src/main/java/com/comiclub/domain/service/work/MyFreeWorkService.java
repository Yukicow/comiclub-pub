package com.comiclub.domain.service.work;


import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.comiclub.domain.entity.board.FreeWorkBoard;
import com.comiclub.domain.entity.freework.FWBackgroundSound;
import com.comiclub.domain.entity.freework.FreeWork;
import com.comiclub.domain.entity.freework.FreeWorkLayer;
import com.comiclub.domain.entity.freework.FreeWorkScene;
import com.comiclub.domain.repository.board.freework.FWBoardRepository;
import com.comiclub.web.contoller.mywork.dto.FreeWorkInfo;
import com.comiclub.web.contoller.mywork.dto.LayerDto;
import com.comiclub.web.contoller.mywork.form.WorkForm;
import com.comiclub.domain.repository.freework.FWBgSoundRepository;
import com.comiclub.domain.repository.freework.FWLayerRepository;
import com.comiclub.domain.repository.freework.FWSceneRepository;
import com.comiclub.domain.repository.freework.FreeWorkRepository;
import com.comiclub.web.exception.AutoModeFalseException;
import com.comiclub.web.exception.NoAuthorizationException;
import com.comiclub.web.exception.NotFoundException;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.amazonaws.services.s3.model.DeleteObjectsRequest.*;
import static com.comiclub.domain.util.constant.WorkConst.*;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class MyFreeWorkService {


    private final FreeWorkRepository workRepository;
    private final FWBgSoundRepository bgSoundRepository;
    private final FWSceneRepository sceneRepository;
    private final FWLayerRepository layerRepository;
    private final FWBoardRepository boardRepository;

    // S3
    private final AmazonS3Client amazonS3Client;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;



    private final String s3Url = "https://comiclub-s3.s3.ap-northeast-2.amazonaws.com/";
    private String getFreeWorkPath(String storeFileName) {
        return "free-work/" + storeFileName;
    }
    private String getFreeWorkUrl(String storeFileName) {
        return s3Url + "free-work/" + storeFileName;
    }
    private String extractKey(String url) {
        return url.substring(s3Url.length());
    }

    private String getFileName(String url) {
        int pos = url.lastIndexOf("/");
        return url.substring(pos + 1);
    }
    private String createFileName(String originFileName) {
        int pos = originFileName.lastIndexOf(".");
        String ext = originFileName.substring(pos);
        return UUID.randomUUID() + ext;
    }



    @Transactional(readOnly = true)
    public Slice<FreeWorkInfo> searchMyFreeWork(Long memberId, String title, Pageable pageable) {
        Slice<FreeWork> workSlice = workRepository.searchMyFreeWork(memberId, title, pageable);
        List<FreeWorkInfo> works = workSlice.stream()
                .map(work -> new FreeWorkInfo(work))
                .collect(Collectors.toList());
        return new SliceImpl<>(works, workSlice.getPageable(), workSlice.hasNext());
    }


    @Transactional(rollbackFor = IOException.class)
    public Long saveFreeWork(Long memberId, WorkForm form, MultipartFile multipartFile) throws IOException {

        FreeWork freeWork = FreeWork.createNewFreeWork(
                memberId,
                form.getTitle(),
                null,
                form.getAutoMode(),
                form.getFreeUse()
        );

        workRepository.save(freeWork);

        // s3 썸네일 업로드
        if(multipartFile != null && !multipartFile.isEmpty()){
            String fileName = createFileName(multipartFile.getOriginalFilename());
            String filePath = getFreeWorkPath(fileName);

            uploadFileToS3(multipartFile, filePath);

            // 썸네일 url을 DB에 업데이트
            freeWork.changeThumbnailUrl(getFreeWorkUrl(fileName));

            /**
             * 혹시나 DB에서 동작에 문제가 생길 경우 저장된 파일을 지워야함. (처리해 주지 않으면 영구 저장됨 )
             * */
            try {
                workRepository.flush();
            }catch (Exception e){
                amazonS3Client.deleteObject(bucket, extractKey(freeWork.getThumbnailUrl()));
                throw e;
            }
        }



        return freeWork.getId();
    }

    @Transactional(rollbackFor = IOException.class)
    public void updateFreeWorkInfo(Long workId, Long memberId, WorkForm form, MultipartFile multipartFile) throws IOException {

        FreeWork freeWork = workRepository.findById(workId)
                .orElseThrow(() -> new NotFoundException("There is no work with id '" + workId + "'"));

        if(!freeWork.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for updating work");

        freeWork.changeTitle(form.getTitle());
        freeWork.changeAutoMode(form.getAutoMode());
        freeWork.changeFreeUse(form.getFreeUse());

        // 썸네일이 변경되었을 때
        if(multipartFile != null && !multipartFile.isEmpty()){
            String fileName = freeWork.getThumbnailUrl() != null
                    ? getFileName(freeWork.getThumbnailUrl())
                    : createFileName(multipartFile.getOriginalFilename());
            String filePath = getFreeWorkPath(fileName);

            uploadFileToS3(multipartFile, filePath);

            freeWork.changeThumbnailUrl(getFreeWorkUrl(fileName));

            // 작품의 게시글의 썸네일도 변경
            FreeWorkBoard freeWorkBoard = boardRepository.findOneByWorkId(freeWork.getId())
                    .orElse(null);

            if(freeWorkBoard != null) freeWorkBoard.changeThumbnailUrl(freeWork.getThumbnailUrl());

            /**
             * 혹시나 DB에서 동작에 문제가 생길 경우 저장된 파일을 지워야함. (처리해 주지 않으면 영구 저장됨 )
             * */
            try {
                workRepository.flush();
            }catch (Exception e){
                amazonS3Client.deleteObject(bucket, extractKey(freeWork.getThumbnailUrl()));
                throw e;
            }

        }
    }

    public void deleteFreeWork(Long workId, Long memberId) {

        FreeWork freeWork = workRepository.findById(workId)
                .orElseThrow(() -> new NotFoundException("There is no work workId with '" + workId + "'"));

        if(!freeWork.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for updating work");

        // 삭제될 모든 파일에 대한 정보를 담음
        ArrayList<KeyVersion> keys = new ArrayList<>();

        // 작품에 대한 모든 파일 추가 (배경음악 및 썸네일)
        if(StringUtils.hasText(freeWork.getThumbnailUrl()))
            keys.add(new KeyVersion(extractKey(freeWork.getThumbnailUrl())));

        freeWork.getBgSounds().stream()
                .filter(bs -> StringUtils.hasText(bs.getFileUrl()))
                .forEach(bs -> keys.add(new KeyVersion(extractKey(bs.getFileUrl()))));

        // 각각의 장면에 해당하는 모든 파일 추가 (레이어 이미지 및 장면 효과음)
        freeWork.getScenes().forEach(scene -> {
            if(StringUtils.hasText(scene.getAudioFileUrl()))
                keys.add(new KeyVersion(extractKey(scene.getAudioFileUrl())));

            scene.getLayers().stream()
                    .filter(layer -> StringUtils.hasText(layer.getImgFileUrl()))
                    .forEach(layer -> keys.add(new KeyVersion(extractKey(layer.getImgFileUrl()))));
        });

        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket)
                .withKeys(keys)
                .withQuiet(false);

        if(keys.size() > 0) amazonS3Client.deleteObjects(deleteObjectsRequest);

        workRepository.delete(freeWork);
    }



    @Transactional(rollbackFor = IOException.class)
    public FWBackgroundSound saveFreeWorkBgSound(Long workId, Long memberId, MultipartFile multipartFile) throws IOException {

        FreeWork freeWork = workRepository.findWithBgSoundById(workId)
                .orElseThrow(() -> new NotFoundException("There is no work with id '" + workId + "'"));

        if (!freeWork.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for creating sound");

        if(!freeWork.getAutoMode())
            throw new AutoModeFalseException("Auto mode is false");

        int nextSoundNum = freeWork.getBgSounds().size() + 1;
        if(nextSoundNum > MAX_BG_SOUND_SIZE)
            throw new ValidationException("The next BackgroundSound number grater than or equal to max size");

        String storeFileName = createFileName(multipartFile.getOriginalFilename());
        FWBackgroundSound bgSound = FWBackgroundSound.createNewFWBackgroundSound(freeWork, memberId, storeFileName, null, 1, 2);

        bgSoundRepository.save(bgSound);

        String filePath = getFreeWorkPath(storeFileName);

        uploadFileToS3(multipartFile, filePath);

        bgSound.changeFileUrl(getFreeWorkUrl(storeFileName));

        /**
         * 혹시나 DB에서 동작에 문제가 생길 경우 저장된 파일을 지워야함. (처리해 주지 않으면 영구 저장됨 )
         * */
        try {
            bgSoundRepository.flush();
        }catch (Exception e){
            amazonS3Client.deleteObject(bucket, extractKey(bgSound.getFileUrl()));
            throw e;
        }

        return bgSound;
    }

    public void deleteFreeWorkBgSound(Long soundId, Long memberId) {

        FWBackgroundSound bgSound = bgSoundRepository.findById(soundId)
                .orElseThrow(() -> new NotFoundException("There is no BackgroundSound"));

        if (!bgSound.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for removing sound");

        if(StringUtils.hasText(bgSound.getFileUrl()))
            amazonS3Client.deleteObject(bucket, extractKey(bgSound.getFileUrl()));

        bgSoundRepository.delete(bgSound);
    }

    public void updateFreeWorkBgSound(Long soundId, Long memberId, int startNum, int endNum) {

        FWBackgroundSound bgSound = bgSoundRepository.findById(soundId)
                .orElseThrow(() -> new NotFoundException("There is no BackgroundSound"));

        if (!bgSound.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for updating sound");

        bgSound.changeStartSceneNumber(startNum);
        bgSound.changeEndSceneNumber(endNum);
    }


    public Long saveFreeWorkScene(Long workId, Long memberId) {
        FreeWork freeWork = workRepository.findWithScenesById(workId)
                .orElseThrow(() -> new NotFoundException("There is no work with id '" + workId + "'"));

        if (!freeWork.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for creating scene");

        int nextSceneNum = sceneRepository.countByFreeWorkId(workId) + 1;
        if (nextSceneNum > MAX_SCENE_SIZE)
            throw new ValidationException("The next number of scene over max size");

        FreeWorkScene scene = FreeWorkScene.createNewFreeWorkScene(freeWork, memberId, nextSceneNum, 1, null, null, 0.0);
        FreeWorkLayer layer = FreeWorkLayer.createNewFreeWorkLayer(scene, memberId, 1, 0.0, null);

        sceneRepository.save(scene);
        layerRepository.save(layer);

        return scene.getId();
    }

    public Long saveFreeWorkSceneToFront(Long workId, Long memberId, int sceneNumber) {
        FreeWork freeWork = workRepository.findWithScenesById(workId)
                .orElseThrow(() -> new NotFoundException("There is no work with id '" + workId + "'"));

        if (!freeWork.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for creating Scene");

        int totalSize = freeWork.getScenes().size();
        if (totalSize >= MAX_SCENE_SIZE)
            throw new ValidationException("Current total size of scene equal to max size");

        FreeWorkScene scene = FreeWorkScene.createNewFreeWorkScene(freeWork, memberId, sceneNumber, 1, null, null, 0.0);
        FreeWorkLayer layer = FreeWorkLayer.createNewFreeWorkLayer(scene, memberId, 1, 0.0, null);

        sceneRepository.increaseSceneNumbers(scene.getFreeWork().getId(), scene.getSceneNumber());

        sceneRepository.save(scene);
        layerRepository.save(layer);

        return scene.getId();
    }

    @Transactional(rollbackFor = IOException.class)
    public void updateFreeWorkSceneAudio(Long sceneId, Long memberId, MultipartFile multipartFile) throws IOException {

        FreeWorkScene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new NotFoundException("There is no scene with id '" + sceneId + "'"));

        if (!scene.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for updating scene");

        if(multipartFile != null && !multipartFile.isEmpty()){
            String fileName = scene.getAudioFileUrl() != null
                    ? getFileName(scene.getAudioFileUrl())
                    : createFileName(multipartFile.getOriginalFilename());
            String filePath = getFreeWorkPath(fileName);

            uploadFileToS3(multipartFile, filePath);

            scene.changeAudioFileUrl(getFreeWorkUrl(fileName));

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

    public void deleteFreeWorkScene(Long sceneId, Long memberId) {

        FreeWorkScene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new NotFoundException("There is no scene with id '" + sceneId + "'"));

        if (!scene.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for removing scene");

        // 삭제될 모든 파일에 대한 정보를 담음
        ArrayList<KeyVersion> keys = new ArrayList<>();

        // 각각의 장면에 해당하는 모든 파일 추가 (레이어 이미지 및 장면 효과음)
        if(StringUtils.hasText(scene.getAudioFileUrl()))
            keys.add(new KeyVersion(extractKey(scene.getAudioFileUrl())));

        scene.getLayers().stream()
                .filter(layer -> StringUtils.hasText(layer.getImgFileUrl()))
                .map(layer -> keys.add(new KeyVersion(extractKey(layer.getImgFileUrl()))));

        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket)
                .withKeys(keys)
                .withQuiet(false);

        if(keys.size() > 0) amazonS3Client.deleteObjects(deleteObjectsRequest);

        sceneRepository.delete(scene);
        sceneRepository.decreaseSceneNumbers(scene.getFreeWork().getId(), scene.getSceneNumber());
    }


    public LayerDto saveFreeWorkLayer(Long sceneId, Long memberId) {
        FreeWorkScene scene = sceneRepository.findById(sceneId)
                .orElseThrow(() -> new NotFoundException("There is no scene with id '" + sceneId + "'"));

        if(!scene.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for creating layer");

        int nextLayerNumber = scene.getTotalLayer() + 1;
        if(nextLayerNumber > MAX_LAYER_SIZE)
            throw new ValidationException("Layer number should be less than 4");

        FreeWorkLayer layer = FreeWorkLayer.createNewFreeWorkLayer(scene, memberId, nextLayerNumber, 0.0,null);

        layer.save();
        layerRepository.save(layer);

        return new LayerDto(layer);
    }

    @Transactional(rollbackFor = IOException.class)
    public void updateFreeWorkLayerImage(Long layerId, Long memberId, MultipartFile multipartFile) throws IOException {

        FreeWorkLayer layer = layerRepository.findById(layerId)
                .orElseThrow(() -> new NotFoundException("There is no Layer with id '"  + layerId + "'"));

        if(!layer.getMemberId().equals(memberId)) throw new NoAuthorizationException("No authorization for updating layer");

        String fileName = layer.getImgFileUrl() != null
                ? getFileName(layer.getImgFileUrl())
                : createFileName(multipartFile.getOriginalFilename());
        String filePath = getFreeWorkPath(fileName);

        uploadFileToS3(multipartFile, filePath);

        layer.changeImageFileUrl(getFreeWorkUrl(fileName));

        int firstLayer = 1;
        if(layer.getLayerNumber().equals(firstLayer)){
            FreeWorkScene scene = sceneRepository.findById(layer.getScene().getId())
                    .orElseThrow(() -> new NotFoundException("There is no scene with id '" + layer.getScene().getId() + "'"));
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

    public void deleteFreeWorkLayer(Long layerId, Long memberId) {

        FreeWorkLayer layer = layerRepository.findWithScenesById(layerId)
                .orElseThrow(() -> new NotFoundException("There is no layer with id'" + layerId + "'"));

        if(!layer.getMemberId().equals(memberId))
            throw new NoAuthorizationException("No authorization for updating layer");

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

        FreeWorkScene scene = sceneRepository.findWithLayersById(sceneId)
                .orElseThrow(() -> new NotFoundException("There is no scene with id '" + sceneId + "'"));

        if(!scene.getMemberId().equals(memberId))
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
