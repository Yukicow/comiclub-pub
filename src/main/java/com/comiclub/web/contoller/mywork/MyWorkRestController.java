package com.comiclub.web.contoller.mywork;


import com.comiclub.domain.entity.freework.*;
import com.comiclub.domain.service.board.EpBoardService;
import com.comiclub.domain.service.board.FWBoardService;
import com.comiclub.web.contoller.board.form.WorkBoardSaveForm;
import com.comiclub.web.contoller.mywork.dto.BgSoundInfo;
import com.comiclub.web.contoller.mywork.dto.LayerDto;
import com.comiclub.web.contoller.mywork.dto.SeriesInfo;
import com.comiclub.web.contoller.mywork.dto.FreeWorkInfo;
import com.comiclub.web.contoller.mywork.form.WorkForm;
import com.comiclub.web.contoller.mywork.form.SeriesForm;
import com.comiclub.web.result.CommonResult;
import com.comiclub.domain.service.work.MySeriesService;
import com.comiclub.domain.service.work.MyFreeWorkService;
import com.comiclub.web.security.CurrentMember;
import com.comiclub.web.security.LoginMember;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ValidationException;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static com.comiclub.domain.util.constant.WorkConst.*;


/**
 * 해당 컨트롤러에 대한 모든 경로는 security에 의해 기본적으로 authentication 과정이 일어나기 때문에 따로 확인할 필요는 없음
 * */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/mywork")
public class MyWorkRestController {



    /**
     * Service
     * */
    private final MyFreeWorkService myFreeWorkService;
    private final FWBoardService fwBoardService;

    private final MySeriesService mySeriesService;
    private final EpBoardService epBoardService;






    /**
     * FreeWork 관련 API
     * */
    
    @GetMapping("/free/lists")
    public ResponseEntity<CommonResult> searchWork(@CurrentMember LoginMember memberInfo,
                                                   @RequestParam(required = false, value = "wt") String workTitle,
                                                   @PageableDefault(sort = "updatedDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Slice<FreeWorkInfo> workSlice = myFreeWorkService.searchMyFreeWork(memberInfo.getId(), workTitle, pageable);

        String code = workSlice.hasNext() ? "HAS_NEXT" : "NO_NEXT";

        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, code, workSlice.getContent()));
    }

    @GetMapping("/series/lists")
    public ResponseEntity<CommonResult> searchSeries(@CurrentMember LoginMember memberInfo,
                                                     @RequestParam(required = false, value = "sn") String seriesName,
                                                     @PageableDefault(size = 2, sort = "updatedDate", direction = Sort.Direction.DESC) Pageable pageable) {

        Slice<SeriesInfo> seriesInfos = mySeriesService.searchMySeries(memberInfo.getId(), seriesName, pageable);

        String code = seriesInfos.hasNext() ? "HAS_NEXT" : "NO_NEXT";

        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, code, seriesInfos.getContent()));
    }

    
    @PostMapping("/free")
    public ResponseEntity<CommonResult> addFreeWork(@CurrentMember LoginMember memberInfo, @RequestParam(name = "thumbnail", required = false) MultipartFile file,
                                                    @Validated @ModelAttribute("work") WorkForm form, BindingResult bindingResult) throws IOException {

        if(bindingResult.hasErrors())
            throw new ValidationException("Validation error has been occurred");

        Long workId = myFreeWorkService.saveFreeWork(memberInfo.getId(), form, file);

        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "REDIRECT", "/mywork/free/" + workId));
    }


    @DeleteMapping("/free/{workId}")
    public ResponseEntity<CommonResult> deleteFreeWork(@CurrentMember LoginMember memberInfo, @PathVariable Long workId) {

        myFreeWorkService.deleteFreeWork(workId, memberInfo.getId());
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/free/{workId}/info")
    public ResponseEntity<CommonResult> editFreeWorkInfo(@CurrentMember LoginMember memberInfo, @PathVariable Long workId,
                                                         @RequestParam(name = "thumbnail", required = false) MultipartFile file,
                                                         @Validated @ModelAttribute("work") WorkForm form, BindingResult bindingResult) throws IOException {

        if(bindingResult.hasErrors())
            throw new ValidationException("Validation error has been occurred");

        myFreeWorkService.updateFreeWorkInfo(workId, memberInfo.getId(), form, file);

        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "REDIRECT", "/mywork"));
    }


    @PostMapping("/free/{workId}/bg-sound")
    public ResponseEntity<CommonResult> addFreeWorkBgSound(@CurrentMember LoginMember memberInfo, @PathVariable Long workId,
                                                    @RequestParam(name = "bgSound") MultipartFile multipartFile) throws IOException {

        if (multipartFile == null || multipartFile.isEmpty())
            throw new ValidationException("File does not exist");

        FWBackgroundSound FWBackgroundSound = myFreeWorkService.saveFreeWorkBgSound(workId, memberInfo.getId(), multipartFile);

        return ResponseEntity
                .ok()
                .body(new CommonResult(
                        HttpServletResponse.SC_OK,
                        "SUCCESS",
                        new BgSoundInfo(FWBackgroundSound)
                ));
    }

    
    @DeleteMapping("/free/bg-sounds/{soundId}")
    public ResponseEntity<CommonResult> deleteFreeWorkBgSound(
            @CurrentMember LoginMember memberInfo, @PathVariable Long soundId) {

        myFreeWorkService.deleteFreeWorkBgSound(soundId, memberInfo.getId());
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/free/bg-sounds/{soundId}")
    public ResponseEntity<CommonResult> updateBgSound(
            @CurrentMember LoginMember memberInfo, @PathVariable Long soundId,
            @RequestParam int startNum, @RequestParam int endNum) {

        validateForUpdateBgSound(startNum, endNum);

        myFreeWorkService.updateFreeWorkBgSound(soundId, memberInfo.getId(), startNum, endNum);

        return ResponseEntity.ok().build();
    }

    private void validateForUpdateBgSound(int startNum, int endNum){
        if(
                endNum - startNum < MIN_SOUND_CONTINUED_NUM ||
                startNum < MIN_SOUND_START_NUM ||
                startNum > MAX_SOUND_START_NUM ||
                endNum < MIN_SOUND_END_NUM ||
                endNum > MAX_SOUND_END_NUM
        ) throw new ValidationException("Not valid parameter for updating BackgroundSound");
    }

    @PostMapping("/free/{workId}/scene")
    public ResponseEntity<CommonResult> addFreeWorkScene(
            @CurrentMember LoginMember memberInfo, @PathVariable Long workId) {

        Long sceneId = myFreeWorkService.saveFreeWorkScene(workId, memberInfo.getId());
        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", sceneId));
    }

    
    @PostMapping("/free/{workId}/scene/front")
    public ResponseEntity<CommonResult> addFreeWorkSceneToFront(
            @CurrentMember LoginMember memberInfo, @PathVariable Long workId, @RequestParam("sno") Integer sceneNumber) {

        Long sceneId = myFreeWorkService.saveFreeWorkSceneToFront(workId, memberInfo.getId(), sceneNumber);
        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", sceneId));
    }

    @DeleteMapping("/free/scenes/{sceneId}")
    public ResponseEntity<CommonResult> deleteFreeWorkScene(@CurrentMember LoginMember memberInfo, @PathVariable Long sceneId) {

        myFreeWorkService.deleteFreeWorkScene(sceneId, memberInfo.getId());
        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", null));
    }

    @PatchMapping("/free/scenes/{sceneId}/audio")
    public ResponseEntity<CommonResult> updateFreeWorkSceneAudio(@CurrentMember LoginMember memberInfo, @PathVariable Long sceneId,
                                                         @RequestParam(name = "audioFile", required = false) MultipartFile multipartFile) throws IOException {

        myFreeWorkService.updateFreeWorkSceneAudio(sceneId, memberInfo.getId(), multipartFile);
        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", null));
    }


    @PostMapping("/free/scenes/{sceneId}/layer")
    public ResponseEntity<CommonResult> createNewFreeWorkLayer(@CurrentMember LoginMember memberInfo, @PathVariable Long sceneId) {

        LayerDto layerDto = myFreeWorkService.saveFreeWorkLayer(sceneId, memberInfo.getId());
        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", layerDto));
    }

    @PatchMapping("/free/scenes/layers/{layerId}/image")
    public ResponseEntity<CommonResult> updateFreeWorkLayerImage(@CurrentMember LoginMember memberInfo, @PathVariable Long layerId,
                                                                 @RequestParam(name = "imgFile") MultipartFile multipartFile) throws IOException {

        if (multipartFile == null || multipartFile.isEmpty()) throw new ValidationException("File does not exist");

        myFreeWorkService.updateFreeWorkLayerImage(layerId, memberInfo.getId(), multipartFile);
        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", null));
    }

    @DeleteMapping("/free/scenes/layers/{layerId}")
    public ResponseEntity<CommonResult> deleteFreeWorkLayer(@CurrentMember LoginMember memberInfo, @PathVariable Long layerId) {

        myFreeWorkService.deleteFreeWorkLayer(layerId, memberInfo.getId());
        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", null));
    }

    @Data
    class LayerDurationMap{
        private Map<Integer, Double> map;
    }

    @PatchMapping("/free/scenes/{sceneId}/layers/duration")
    public ResponseEntity<CommonResult> updateFreeWorkLayersDuration(@CurrentMember LoginMember memberInfo,
                                                             @PathVariable Long sceneId, @RequestBody Map<Long, Double> durationMap) {

        myFreeWorkService.updateDurations(sceneId, memberInfo.getId(), durationMap);
        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", null));
    }








    
    /**
     * Series관련 API
     * */

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/series")
    public ResponseEntity<CommonResult> addSeriesWork(@CurrentMember LoginMember memberInfo, @RequestParam(name = "thumbnail", required = false) MultipartFile file,
                                                      @Validated @ModelAttribute("series") SeriesForm form, BindingResult bindingResult) throws IOException {

        if(bindingResult.hasErrors())
            throw new ValidationException("Validation error has been occurred");

        Long seriesId = mySeriesService.saveSeries(memberInfo.getId(), memberInfo.getNickname(), form, file);
        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "REDIRECT", "/mywork/series/" + seriesId));
    }

    @DeleteMapping("/series/{id}")
    public ResponseEntity<Object> deleteSeries(@CurrentMember LoginMember memberInfo, @PathVariable Long id){
        mySeriesService.deleteSeries(id, memberInfo.getId());
        return ResponseEntity.ok().build();
    }
    
    @PatchMapping("/series/{id}/info")
    public ResponseEntity<CommonResult> editSeriesInfo(@CurrentMember LoginMember memberInfo, @PathVariable Long id,
                                                       @ModelAttribute("series") SeriesForm form,
                                                       @RequestParam(name = "thumbnail", required = false) MultipartFile multipartFile) throws IOException {

        mySeriesService.updateSeries(id, memberInfo.getId(), form, multipartFile);
        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "REDIRECT", "/mywork/series/" + id));
    }


    @PostMapping("/series/{seriesId}/episode")
    public ResponseEntity<CommonResult> addEpisode(@PathVariable Long seriesId,
                                                   @CurrentMember LoginMember memberInfo,
                                                   @Validated @ModelAttribute("episode") WorkForm form, BindingResult bindingResult,
                                                   @RequestParam(name = "thumbnail", required = false) MultipartFile multipartFile) throws IOException {

        if(bindingResult.hasErrors()) throw new ValidationException("Validation error has been occurred");

        mySeriesService.saveEpisode(seriesId, memberInfo.getId(), form, multipartFile);
        return ResponseEntity
                .ok()
                .body(new CommonResult(
                        HttpServletResponse.SC_OK, "REDIRECT", "/mywork/series/" + seriesId));
    }


    @DeleteMapping("/episodes/{episodeId}")
    public ResponseEntity<CommonResult> deleteEpisode(@CurrentMember LoginMember memberInfo, @PathVariable Long episodeId) {

        Long seriesId = mySeriesService.deleteEpisode(episodeId, memberInfo.getId());
        return ResponseEntity
                .ok()
                .body(new CommonResult<>(HttpServletResponse.SC_OK, "REDIRECT", "/mywork/series/" + seriesId));
    }


    @PatchMapping("/episodes/{episodeId}/info")
    public ResponseEntity<Object> editEpisodeInfo(@PathVariable Long episodeId,
                                                  @RequestParam(name = "thumbnail", required = false) MultipartFile multipartFile,
                                                  @CurrentMember LoginMember memberInfo,
                                                  @Validated @ModelAttribute("episode") WorkForm form, BindingResult bindingResult) throws IOException {

        if(bindingResult.hasErrors()) throw new ValidationException("Validation error has been occurred");

        mySeriesService.updateEpisode(episodeId, memberInfo.getId(), form, multipartFile);

        return ResponseEntity
                .ok()
                .body(new CommonResult<>(
                        HttpServletResponse.SC_OK,
                        "REDIRECT",
                        "/mywork/episodes/" + episodeId
                ));
    }


    @PostMapping("/episodes/{episodeId}/bg-sound")
    public ResponseEntity<CommonResult> addEpisodeBgSound(@CurrentMember LoginMember memberInfo, @PathVariable Long episodeId,
                                                   @RequestParam(name = "bgSound") MultipartFile multipartFile) throws IOException {

        if (multipartFile == null || multipartFile.isEmpty()) throw new ValidationException("File does not exist");

        BgSoundInfo bgSoundInfo = mySeriesService.saveEpisodeBgSound(episodeId, memberInfo.getId(), multipartFile);

        return ResponseEntity
                .ok()
                .body(new CommonResult(
                        HttpServletResponse.SC_OK,
                        "SUCCESS", bgSoundInfo
                ));
    }

    
    @DeleteMapping("/episodes/bg-sounds/{soundId}")
    public ResponseEntity<CommonResult> deleteEpisodeBgSound(
            @CurrentMember LoginMember memberInfo, @PathVariable Long soundId){

        mySeriesService.deleteEpisodeBgSound(soundId, memberInfo.getId());
        return ResponseEntity.ok().build();
    }


    @PatchMapping("/episodes/bg-sounds/{soundId}")
    public ResponseEntity<CommonResult> updateEpisodeBgSound(
            @CurrentMember LoginMember memberInfo, @PathVariable Long soundId,
            @RequestParam int startNum, @RequestParam int endNum) {

        validateForUpdateBgSound(startNum, endNum);

        mySeriesService.updateEpisodeBgSound(soundId, memberInfo.getId(), startNum, endNum);

        return ResponseEntity.ok().build();

    }

    
    @PostMapping("/episodes/{episodeId}/scene")
    public ResponseEntity<CommonResult> addEpisodeScene(@CurrentMember LoginMember memberInfo,
                                                               @PathVariable Long episodeId) {

        Long sceneId = mySeriesService.saveEpisodeScene(episodeId, memberInfo.getId());
        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", sceneId));
    }

    @PostMapping("/episodes/{episodeId}/scene/front")
    public ResponseEntity<CommonResult> addEpisodeSceneToFront(@CurrentMember LoginMember memberInfo,
                                                               @PathVariable Long episodeId, @RequestParam("sno") Integer sceneNumber) {

        Long sceneId = mySeriesService.saveEpisodeSceneToFront(episodeId, memberInfo.getId(), sceneNumber);
        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", sceneId));
    }

    
    @DeleteMapping("/episodes/scenes/{sceneId}")
    public ResponseEntity<CommonResult> deleteEpisodeScene(@CurrentMember LoginMember memberInfo, @PathVariable Long sceneId) {

        mySeriesService.deleteEpisodeScene(sceneId, memberInfo.getId());
        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", null));
    }

    @PatchMapping("/episodes/scenes/{sceneId}/audio")
    public ResponseEntity<CommonResult> updateEpisodeSceneAudio(@CurrentMember LoginMember memberInfo, @PathVariable Long sceneId,
                                                                @RequestParam(name = "audioFile", required = false) MultipartFile multipartFile) throws IOException {

        mySeriesService.updateEpisodeSceneAudio(sceneId, memberInfo.getId(), multipartFile);
        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", null));
    }


    @PostMapping("/episodes/scenes/{sceneId}/layer")
    public ResponseEntity<CommonResult> createNewEpisodeLayer(@CurrentMember LoginMember memberInfo, @PathVariable Long sceneId){

        LayerDto layerDto = mySeriesService.saveEpisodeLayer(sceneId, memberInfo.getId());
        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", layerDto));
    }

    @PatchMapping("/episodes/scenes/layers/{layerId}/image")
    public ResponseEntity<CommonResult> updateEpisodeLayerImage(@CurrentMember LoginMember memberInfo, @PathVariable Long layerId,
                                                                @RequestParam(name = "imgFile") MultipartFile multipartFile) throws IOException {

        if (multipartFile == null || multipartFile.isEmpty()) throw new ValidationException("File does not exist");

        mySeriesService.updateEpisodeLayerImage(layerId, memberInfo.getId(), multipartFile);

        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", null));
    }

    @PatchMapping("/episodes/scenes/{sceneId}/layers/duration")
    public ResponseEntity<CommonResult> updateEpisodeDuration(@CurrentMember LoginMember memberInfo,
                                                              @PathVariable Long sceneId, @RequestBody Map<Long, Double> durationMap) {

        mySeriesService.updateDurations(sceneId, memberInfo.getId(), durationMap);
        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", null));
    }


    @DeleteMapping("/episodes/scenes/layers/{layerId}")
    public ResponseEntity<CommonResult> deleteEpisodeLayer(@CurrentMember LoginMember memberInfo, @PathVariable Long layerId) {

        mySeriesService.deleteEpisodeLayer(layerId, memberInfo.getId());
        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "SUCCESS", null));
    }
    
    
    
    
    
    /**
     * Board 옵션
     * */


    // Free Work
    @PostMapping("/free/{workId}/board")
    public ResponseEntity<CommonResult> addFreeWorkBoard(@CurrentMember LoginMember memberInfo, @PathVariable Long workId,
                                                     @Validated @ModelAttribute("form") WorkBoardSaveForm form, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) throw new ValidationException("Validation error has been occurred");

        fwBoardService.saveWorkBoard(form, workId, memberInfo.getId(), memberInfo.getNickname());

        String redirectUrl = "/mywork/free/" + workId;

        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "REDIRECT", redirectUrl));
    }

    @DeleteMapping("/free/{workId}/board")
    public ResponseEntity<CommonResult> deleteFreeWorkBoard(@CurrentMember LoginMember memberInfo, @PathVariable Long workId) {

        Long freeWorkId = fwBoardService.deleteWorkBoard(workId, memberInfo.getId());

        String redirectUrl = "/mywork/free/" + freeWorkId;

        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "REDIRECT", redirectUrl));
    }

    @PatchMapping("/free/{workId}/board/info")
    public ResponseEntity<CommonResult> editFreeWorkBoard(@CurrentMember LoginMember memberInfo, @PathVariable Long workId,
                                                        @ModelAttribute("form") WorkBoardSaveForm form, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) throw new ValidationException("Validation error has been occurred");

        Long freeWorkId = fwBoardService.updateWorkBoard(form, workId, memberInfo.getId());

        String redirectUrl = "/mywork/free/" + freeWorkId;

        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "REDIRECT", redirectUrl));
    }







    // Episode
    @PostMapping("/episodes/{episodeId}/board")
    public ResponseEntity<CommonResult> addEpisodeBoard(@CurrentMember LoginMember memberInfo, @PathVariable Long episodeId,
                                                        @Validated @ModelAttribute("form") WorkBoardSaveForm form, BindingResult bindingResult) {

        if (bindingResult.hasErrors())
            throw new ValidationException("Validation error has been occurred");

        epBoardService.saveEpisodeBoard(form, episodeId, memberInfo.getId(), memberInfo.getNickname());

        String redirectUrl = "/mywork/episodes/" + episodeId;

        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "REDIRECT", redirectUrl));
    }

    @DeleteMapping("/episodes/{episodeId}/board")
    public ResponseEntity<CommonResult> deleteEpisodeBoard(@CurrentMember LoginMember memberInfo, @PathVariable Long episodeId) {

        epBoardService.deleteEpisodeBoard(episodeId, memberInfo.getId());

        String redirectUrl = "/mywork/episodes/" + episodeId;

        return ResponseEntity
                .ok()
                .body(new CommonResult(HttpServletResponse.SC_OK, "REDIRECT", redirectUrl));
    }

    @PatchMapping("/episodes/{episodeId}/board/info")
    public ResponseEntity<Object> editEpisodeBoard(@CurrentMember LoginMember memberInfo, @PathVariable Long episodeId,
                                                         @ModelAttribute("form") WorkBoardSaveForm form) {

        epBoardService.updateEpisodeBoard(form, episodeId, memberInfo.getId());
        return ResponseEntity.ok().build();
    }



}
