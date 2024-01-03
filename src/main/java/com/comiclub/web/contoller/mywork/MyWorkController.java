package com.comiclub.web.contoller.mywork;


import com.comiclub.domain.entity.board.EpisodeBoard;
import com.comiclub.domain.entity.board.FreeWorkBoard;
import com.comiclub.domain.entity.freework.FreeWork;
import com.comiclub.domain.entity.freework.FreeWorkScene;
import com.comiclub.domain.entity.freework.FreeWorkLayer;
import com.comiclub.domain.entity.sereis.Episode;
import com.comiclub.domain.entity.sereis.EpisodeLayer;
import com.comiclub.domain.entity.sereis.EpisodeScene;
import com.comiclub.domain.entity.sereis.Series;
import com.comiclub.domain.repository.board.episode.EBoardRepository;
import com.comiclub.domain.repository.board.freework.FWBoardRepository;
import com.comiclub.domain.repository.freework.FWLayerRepository;
import com.comiclub.domain.repository.freework.FWSceneRepository;
import com.comiclub.domain.repository.series.*;
import com.comiclub.domain.repository.freework.FreeWorkRepository;
import com.comiclub.domain.service.work.MySeriesService;
import com.comiclub.domain.service.work.MyFreeWorkService;
import com.comiclub.web.contoller.board.form.WorkBoardSaveForm;
import com.comiclub.web.contoller.mywork.dto.*;
import com.comiclub.web.contoller.mywork.dto.edit.*;
import com.comiclub.web.contoller.mywork.form.SeriesForm;
import com.comiclub.web.contoller.mywork.form.WorkForm;
import com.comiclub.web.exception.AlreadyExistException;
import com.comiclub.web.exception.NoAuthorizationException;
import com.comiclub.web.exception.NotFoundException;
import com.comiclub.web.security.CurrentMember;
import com.comiclub.web.security.LoginMember;
import jakarta.validation.ValidationException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;



@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/mywork")
public class MyWorkController {



    /**
     * Repository
     * */

    // Episode
    private final FreeWorkRepository freeWorkRepository;

    private final FWSceneRepository fwSceneRepository;
    private final FWBoardRepository fwBoardRepository;

    // Series
    private final SeriesRepository seriesRepository;
    private final EpisodeRepository episodeRepository;
    private final EpSceneRepository epSceneRepository;
    private final EBoardRepository eBoardRepository;

    /**
     * Service
     * */
    private final MyFreeWorkService myFreeWorkService;
    private final MySeriesService mySeriesService;



    /**
     * FreeWork 관련 API
     * */

    @GetMapping("")
    public String myWorkPage(@CurrentMember LoginMember member, Model model){

        PageRequest pageRequest = PageRequest.of(0, 10, Sort.by("updatedDate").descending());

        Slice<FreeWorkInfo> workInfos = myFreeWorkService.searchMyFreeWork(member.getId(), "", pageRequest);
        Slice<SeriesInfo> seriesInfos = mySeriesService.searchMySeries(member.getId(), "", pageRequest);

        model.addAttribute("freeWorks", workInfos);
        model.addAttribute("seriesSlice", seriesInfos);
        return "views/mywork/works";
    }

    @GetMapping("/free")
    public String freeWorkAddPage(@ModelAttribute("freeWork") WorkForm workForm){
        return "views/mywork/add_free_work";
    }

    @GetMapping("/free/{workId}")
    public String freeWorkModifyPage(@CurrentMember LoginMember memberInfo, @PathVariable Long workId, Model model){

        // JPA에서 두 개의 OneToMany fetch join은 지원하지 않으므로 하나만 join하고 하나는 lazy로딩
        FreeWork freeWork = freeWorkRepository.findWithScenesById(workId)
                .orElseThrow(() -> new NotFoundException("There is no work workId '" + workId + "'"));

        if(!memberInfo.getId().equals(freeWork.getMemberId()))
            throw new NoAuthorizationException("No authorization for edit work");

        List<SceneViewDto> scenes = freeWork.getScenes().stream()
                .map(scene -> new SceneViewDto(scene))
                .sorted(Comparator.comparing(SceneViewDto::getSceneNumber))
                .collect(Collectors.toList());

        List<BackgroundSoundDto> bgSounds = freeWork.getBgSounds().stream()
                .map(bgSound -> new BackgroundSoundDto(bgSound))
                .collect(Collectors.toList());

        WorkEditDto workInfo = new WorkEditDto(freeWork, scenes, bgSounds);

        boolean boardExist = fwBoardRepository.existsByFreeWorkId(workId);

        workInfo.setScenes(scenes);
        workInfo.setBgSounds(bgSounds);
        model.addAttribute("freeWork", workInfo);
        model.addAttribute("boardExist", boardExist);
        return "views/mywork/edit_free_work";
    }

    @GetMapping("/free/{workId}/info")
    public String freeWorkInfoModifyPage(@CurrentMember LoginMember memberInfo, @PathVariable Long workId,
                                       @ModelAttribute("freeWork") WorkForm form, Model model) {

        FreeWork freeWork = freeWorkRepository.findWithScenesById(workId)
                .orElseThrow(() -> new NotFoundException("There is no work workId '" + workId + "'"));

        if(!memberInfo.getId().equals(freeWork.getMemberId()))
            throw new NoAuthorizationException("No authorization for edit work");

        form.setTitle(freeWork.getTitle());
        form.setAutoMode(freeWork.getAutoMode());
        form.setFreeUse(freeWork.getFreeUse());

        model.addAttribute("thumbnailUrl", freeWork.getThumbnailUrl());
        model.addAttribute("freeWorkId", workId);
        return "views/mywork/edit_free_work_info";
    }

    @GetMapping("/free/{workId}/scene")
    public String freeWorkSceneModifyPage(@CurrentMember LoginMember memberInfo, @PathVariable Long workId,
                                          @RequestParam("sno") int sceneNum, Model model) {

        List<FreeWorkScene> scenes = fwSceneRepository.findScenesByRangeNumber(workId, sceneNum);
        FreeWorkScene scene = scenes.stream()
                .collect(Collectors.toMap(
                        FreeWorkScene::getSceneNumber,
                        Function.identity()
                ))
                .get(sceneNum);

        if(scene == null)
            throw new NotFoundException("There is no scene");

        if(!memberInfo.getId().equals(scene.getMemberId()))
            throw new NoAuthorizationException("No authorization for edit work");

        boolean hasNextScene = false;
        boolean hasPrevScene = false;
        for (FreeWorkScene s: scenes) {
            if(s.getSceneNumber() == scene.getSceneNumber() + 1)
                hasNextScene = true;
            else if(s.getSceneNumber() == scene.getSceneNumber() - 1)
                hasPrevScene = true;
        }

        SceneDto sceneDto = new SceneDto(scene);

        model.addAttribute("scene", sceneDto);
        model.addAttribute("hasNextScene", hasNextScene);
        model.addAttribute("hasPrevScene", hasPrevScene);
        model.addAttribute("workUrl", "/mywork/free/" + workId);
        return "views/mywork/edit_scene";
    }

    @GetMapping("/free/{workId}/scene/audio")
    public String sceneAudioModifyPage(@CurrentMember LoginMember memberInfo, @PathVariable Long workId,
                                       @RequestParam("sno") int sceneNum, Model model) {


        List<FreeWorkScene> scenes = fwSceneRepository.findScenesByRangeNumber(workId, sceneNum);
        FreeWorkScene scene = scenes.stream()
                .collect(Collectors.toMap(
                        FreeWorkScene::getSceneNumber,
                        Function.identity()
                ))
                .get(sceneNum);

        if(scene == null)
            throw new NotFoundException("There is no scene");

        if(!scene.getMemberId().equals(memberInfo.getId()))
            throw new NoAuthorizationException("No authorization for edit work");

        boolean hasNextScene = false;
        boolean hasPrevScene = false;
        for (FreeWorkScene s: scenes) {
            if(s.getSceneNumber() == scene.getSceneNumber() + 1)
                hasNextScene = true;
            else if(s.getSceneNumber() == scene.getSceneNumber() - 1)
                hasPrevScene = true;
        }

        SceneDto sceneDto = new SceneDto(scene);

        model.addAttribute("scene", sceneDto);
        model.addAttribute("hasNextScene", hasNextScene);
        model.addAttribute("hasPrevScene", hasPrevScene);
        model.addAttribute("workUrl", "/mywork/free/" + workId);
        return "views/mywork/edit_audio";
    }


    @GetMapping("/free/{workId}/scenes/{sceneId}/layers/{layerId}")
    public String freeWorkLayerModifyPage(@CurrentMember LoginMember memberInfo, @PathVariable Long workId,
                                          @PathVariable Long sceneId, @PathVariable Long layerId, Model model) {

        FreeWorkScene scene = fwSceneRepository.findWithLayersById(sceneId)
                .orElseThrow(() -> new NotFoundException("There is no scene id" + sceneId + "'"));

        List<FreeWorkLayer> layers = scene.getLayers();
        FreeWorkLayer layer = layers.stream()
                .collect(Collectors.toMap(
                        FreeWorkLayer::getId,
                        Function.identity()
                )).get(layerId);

        if(!layer.getMemberId().equals(memberInfo.getId()))
            throw new NoAuthorizationException("No authorization for edit work");

        Long nextLayerId = null;
        Long prevLayerId = null;
        for (FreeWorkLayer l : layers) {
            if(l.getLayerNumber() == layer.getLayerNumber() + 1)
                nextLayerId = l.getId();
            else if(l.getLayerNumber() == layer.getLayerNumber() - 1)
                prevLayerId = l.getId();
        }

        LayerDto layerDto = new LayerDto(layer);

        model.addAttribute("layer", layerDto);
        model.addAttribute("sceneId", sceneId);
        model.addAttribute("sceneNumber", scene.getSceneNumber());
        model.addAttribute("nextLayerId", nextLayerId);
        model.addAttribute("prevLayerId", prevLayerId);
        model.addAttribute("workUrl", "/mywork/free/" + workId);
        return "views/mywork/edit_layer";
    }


    @GetMapping("/free/{workId}/board")
    public String freeWorkBoardAddPage(@CurrentMember LoginMember memberInfo, @PathVariable Long workId,
                                       @ModelAttribute("form") WorkBoardSaveForm form) {

        if(fwBoardRepository.existsByFreeWorkId(workId))
            throw new AlreadyExistException("The board is already exist");

        FreeWork freeWork = freeWorkRepository.findById(workId)
                .orElseThrow(() -> new NotFoundException("There is no work id '" + workId + "'"));

        if (!memberInfo.getId().equals(freeWork.getMemberId()))
            throw new NoAuthorizationException("No authorization for removing scene");

        form.setTitle(freeWork.getTitle());
        form.setIsPublic(true);
        return "views/mywork/add_board";
    }

    @GetMapping("/free/{workId}/board/info")
    public String freeWorkBoardModifyPage(@CurrentMember LoginMember memberInfo, @PathVariable Long workId,
                                        @ModelAttribute("form") WorkBoardSaveForm form) {

        FreeWorkBoard board = fwBoardRepository.findOneByWorkId(workId)
                .orElseThrow(() -> new NotFoundException("There is no WorkBoard"));

        if (!memberInfo.getId().equals(board.getMemberId()))
            throw new NoAuthorizationException("No authorization for removing scene");

        form.setTitle(board.getTitle());
        form.setAuthorWord(board.getAuthorWords());
        form.setAdultOnly(board.getAdultOnly());
        form.setIsPublic(board.getPub());
        return "views/mywork/edit_board";
    }




    /**
     * Series관련 API
     * */

    @GetMapping("/series")
    public String seriesAddPage(@ModelAttribute("series") SeriesForm form){
        return "views/mywork/add_series_work";
    }

    @GetMapping("/series/{seriesId}")
    public String seriesDetailPage(@PathVariable Long seriesId, @CurrentMember LoginMember memberInfo,
                         @PageableDefault(size = 2, sort = "createdDate", direction = Sort.Direction.DESC) Pageable pageable, Model model){

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new NotFoundException("There is no series id '" + seriesId + "'"));

        if (!memberInfo.getId().equals(series.getMemberId()))
            throw new NoAuthorizationException("No authorization for opening series");

        SeriesEditDto seriesInfo = new SeriesEditDto(series);

        Page<Episode> page = episodeRepository.findBySeriesId(seriesId, pageable);
        List<EpisodeInfo> episodes = page.stream()
                .map(episode -> new EpisodeInfo(episode))
                .collect(Collectors.toList());
        seriesInfo.setEpisodes(episodes);

        Page<EpisodeInfo> resultPage = PageableExecutionUtils.getPage(episodes, pageable, () -> page.getTotalElements());

        model.addAttribute("series", seriesInfo);
        model.addAttribute("page", resultPage);
        return "views/mywork/edit_series";
    }

    @GetMapping("/series/{seriesId}/info")
    public String seriesInfoModifyPage(@CurrentMember LoginMember memberInfo, @PathVariable Long seriesId,
                                     @ModelAttribute("series") SeriesForm form, Model model){

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new NotFoundException("There is no series id '" + seriesId + "'"));

        if (!memberInfo.getId().equals(series.getMemberId()))
            throw new NoAuthorizationException("No authorization for editing series");

        form.setName(series.getName());
        form.setDescription(series.getDescription());
        form.setAdultOnly(series.getAdultOnly());
        form.setTag(series.getTag());
        form.setPub(series.getPub());

        model.addAttribute("thumbnailUrl", series.getThumbnailUrl());
        model.addAttribute("seriesId", series.getId());
        return "views/mywork/edit_series_info";
    }

    @GetMapping("/series/{seriesId}/episode")
    public String episodeAddPage(@CurrentMember LoginMember memberInfo, @PathVariable Long seriesId,
                                 @ModelAttribute("episode") WorkForm form, Model model){

        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new NotFoundException("There is no series id '" + seriesId + "'"));

        if (!memberInfo.getId().equals(series.getMemberId()))
            throw new NoAuthorizationException("No authorization for creating episode");

        model.addAttribute("seriesName", series.getName());
        model.addAttribute("nextEp", series.getTotalEp() + 1);
        model.addAttribute("seriesId", series.getId());
        return "views/mywork/add_series_episode";
    }

    @GetMapping("/episodes/{episodeId}")
    public String episodeModifyPage(@CurrentMember LoginMember memberInfo, @PathVariable Long episodeId, Model model){

        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new NotFoundException("There is no episode id '" + episodeId + "'"));

        if (!memberInfo.getId().equals(episode.getMemberId()))
            throw new NoAuthorizationException("No authorization for opening episode");

        List<SceneViewDto> scenes = episode.getScenes().stream()
                .map(scene -> new SceneViewDto(scene))
                .sorted(Comparator.comparing(SceneViewDto::getSceneNumber))
                .collect(Collectors.toList());

        List<BackgroundSoundDto> bgSounds = episode.getBgSounds().stream()
                .map(bgSound -> new BackgroundSoundDto(bgSound))
                .collect(Collectors.toList());

        boolean boardExist = eBoardRepository.existsByEpisodeId(episodeId);

        WorkEditDto workInfo = new WorkEditDto(episode, scenes, bgSounds);

        workInfo.setScenes(scenes);
        workInfo.setBgSounds(bgSounds);
        model.addAttribute("episode", workInfo);
        model.addAttribute("boardExist", boardExist);
        return "views/mywork/edit_series_episode";
    }

    @GetMapping("/episodes/{episodeId}/info")
    public String episodeInfoModifyPage(@CurrentMember LoginMember memberInfo, @PathVariable Long episodeId,
                                      @ModelAttribute("episode") WorkForm form, Model model){

        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new NotFoundException("There is no episode id '" + episodeId + "'"));

        Series series = seriesRepository.findById(episode.getSeriesId())
                .orElseThrow(() -> new NotFoundException("There is no series id '" + episode.getSeriesId() + "'"));

        if (!memberInfo.getId().equals(episode.getMemberId()))
            throw new NoAuthorizationException("No authorization for editing work");

        form.setTitle(episode.getTitle());
        form.setAutoMode(episode.getAutoMode());
        form.setFreeUse(episode.getFreeUse());

        @Data
        @AllArgsConstructor
        class EpisodeEditForm {
            Integer episodeNumber;
            String seriesName;
            String thumbnailUrl;
        }

        model.addAttribute("editInfo", new EpisodeEditForm(
                episode.getEpisodeNumber(),
                series.getName(),
                episode.getThumbnailUrl()
        ));
        return "views/mywork/edit_series_episode_info";
    }

    @GetMapping("/episodes/{episodeId}/scene")
    public String episodeSceneModifyPage(@CurrentMember LoginMember memberInfo, @PathVariable Long episodeId,
                                         @RequestParam("sno") int sceneNum, Model model) {

        List<EpisodeScene> scenes = epSceneRepository.findScenesByRangeNumber(episodeId, sceneNum);
        EpisodeScene scene = scenes.stream()
                .collect(Collectors.toMap(
                        EpisodeScene::getSceneNumber,
                        Function.identity()
                ))
                .get(sceneNum);

        if(scene == null)
            throw new NotFoundException("There is no scene");

        if(!scene.getMemberId().equals(memberInfo.getId()))
            throw new NoAuthorizationException("No authorization for editing work");

        boolean hasNextScene = false;
        boolean hasPrevScene = false;
        for (EpisodeScene s: scenes) {
            if(s.getSceneNumber() == scene.getSceneNumber() + 1)
                hasNextScene = true;
            else if(s.getSceneNumber() == scene.getSceneNumber() - 1)
                hasPrevScene = true;
        }

        SceneDto sceneDto = new SceneDto(scene);

        model.addAttribute("scene", sceneDto);
        model.addAttribute("hasNextScene", hasNextScene);
        model.addAttribute("hasPrevScene", hasPrevScene);
        model.addAttribute("workUrl", "/mywork/episodes/" + episodeId);
        return "views/mywork/edit_scene";
    }

    @GetMapping("/episodes/{episodeId}/scene/audio")
    public String episodeAudioModifyPage(@CurrentMember LoginMember memberInfo, @PathVariable Long episodeId,
                                         @RequestParam("sno") int sceneNum, Model model) {

        List<EpisodeScene> scenes = epSceneRepository.findScenesByRangeNumber(episodeId, sceneNum);
        EpisodeScene scene = scenes.stream()
                .collect(Collectors.toMap(
                        EpisodeScene::getSceneNumber,
                        Function.identity()
                ))
                .get(sceneNum);

        if(scene == null)
            throw new NotFoundException("There is no scene");

        if(!scene.getMemberId().equals(memberInfo.getId()))
            throw new NoAuthorizationException("No authorization for edit work");

        boolean hasNextScene = false;
        boolean hasPrevScene = false;
        for (EpisodeScene s: scenes) {
            if(s.getSceneNumber() == scene.getSceneNumber() + 1)
                hasNextScene = true;
            else if(s.getSceneNumber() == scene.getSceneNumber() - 1)
                hasPrevScene = true;
        }

        SceneDto sceneDto = new SceneDto(scene);

        model.addAttribute("scene", sceneDto);
        model.addAttribute("hasNextScene", hasNextScene);
        model.addAttribute("hasPrevScene", hasPrevScene);
        model.addAttribute("workUrl", "/mywork/episodes/" + episodeId);
        return "views/mywork/edit_audio";
    }


    @GetMapping("/episodes/{episodeId}/scenes/{sceneId}/layers/{layerId}")
    public String episodeLayerModifyPage(@CurrentMember LoginMember memberInfo, @PathVariable Long episodeId,
                                         @PathVariable Long sceneId, @PathVariable Long layerId, Model model) {

        EpisodeScene scene = epSceneRepository.findWithLayersById(sceneId)
                .orElseThrow(() -> new NotFoundException("There is no scene id '" + sceneId + "'"));

        List<EpisodeLayer> layers = scene.getLayers();
        EpisodeLayer layer = layers.stream()
                .collect(Collectors.toMap(
                        EpisodeLayer::getId,
                        Function.identity()
                )).get(layerId);

        if(!memberInfo.getId().equals(layer.getMemberId()))
            throw new NoAuthorizationException("No authorization for edit work");


        Long nexLayerId = null;
        Long prevLayerId = null;
        for (int i = 0; i < layers.size(); i++) {
            EpisodeLayer l = layers.get(i);
            if(l.getLayerNumber() == layer.getLayerNumber() + 1)
                nexLayerId = l.getId();
            else if(l.getLayerNumber() == layer.getLayerNumber() - 1)
                prevLayerId = l.getId();
        }

        LayerDto layerDto = new LayerDto(layer);

        model.addAttribute("layer", layerDto);
        model.addAttribute("sceneId", sceneId);
        model.addAttribute("sceneNumber", scene.getSceneNumber());
        model.addAttribute("nextLayerId", nexLayerId);
        model.addAttribute("prevLayerId", prevLayerId);
        model.addAttribute("workUrl", "/mywork/episodes/" + episodeId);
        return "views/mywork/edit_layer";
    }

    @GetMapping("/episodes/{episodeId}/board")
    public String episodeBoardAddPage(@CurrentMember LoginMember memberInfo, @PathVariable Long episodeId,
                                       @ModelAttribute("form") WorkBoardSaveForm form) {

        if(eBoardRepository.existsByEpisodeId(episodeId))
            throw new AlreadyExistException("The board is already exist");

        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new NotFoundException("There is no episode id '" + episodeId + "'"));

        if (!memberInfo.getId().equals(episode.getMemberId()))
            throw new NoAuthorizationException("No authorization for removing scene");

        form.setTitle(episode.getTitle());
        form.setIsPublic(true);
        return "views/mywork/add_board";
    }

    @GetMapping("/episodes/{episodeId}/board/info")
    public String episodeBoardModifyPage(@CurrentMember LoginMember memberInfo, @PathVariable Long episodeId,
                                        @ModelAttribute("form") WorkBoardSaveForm form) {

        EpisodeBoard board = eBoardRepository.findOneByEpisodeId(episodeId)
                .orElseThrow(() -> new NotFoundException("There is no EpisodeBoard"));

        if (!memberInfo.getId().equals(board.getMemberId()))
            throw new NoAuthorizationException("No authorization for removing scene");

        form.setTitle(board.getTitle());
        form.setAuthorWord(board.getAuthorWords());
        form.setAdultOnly(board.getAdultOnly());
        form.setIsPublic(board.getPub());
        return "views/mywork/edit_board";
    }








}
