package com.sesa.medical.utilities.service;

import com.sesa.medical.utilities.entities.GeneralCondition;
import com.sesa.medical.utilities.entities.PrivacyPolicy;
import com.sesa.medical.utilities.entities.SliderDocument;
import com.sesa.medical.utilities.entities.YoutubeVideo;
import com.sesa.medical.utilities.repository.GeneralConditionRepo;
import com.sesa.medical.utilities.repository.PrivacyPolicyRepo;
import com.sesa.medical.utilities.repository.SliderDocumentRepo;
import com.sesa.medical.utilities.repository.YoutubeVideoRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UtilitieServiceImpl implements UtilitieService {

    @Autowired
    GeneralConditionRepo generalConditionRepo;
    @Autowired
    PrivacyPolicyRepo privacyPolicyRepo;

    @Autowired
    SliderDocumentRepo sliderDocumentRepo;

    @Autowired
    YoutubeVideoRepo youtubeVideoRepo;

    @Override
    public PrivacyPolicy getprivacyPolicy() {
        return privacyPolicyRepo.findById(1L).orElseThrow(() -> new ResourceNotFoundException("Privacy Policy Not Found"));
    }

    @Override
    public GeneralCondition getGeneralCondition() {
        return generalConditionRepo.findById(1L).orElseThrow(() -> new ResourceNotFoundException("General condition not found"));
    }

    @Override
    public SliderDocument createSlider(SliderDocument sliderDocument) {
        return sliderDocumentRepo.save(sliderDocument);
    }

    @Override
    public List<SliderDocument> getAllSlider() {
        return sliderDocumentRepo.findAll();
    }

    @Override
    public void deleteSliderDocument(Long sliderId) {
      SliderDocument doc = sliderDocumentRepo.findById(sliderId).orElseThrow(() -> new ResourceNotFoundException("Slider document where id: "+sliderId+" not found"));
      sliderDocumentRepo.delete(doc);
    }

    @Override
    public YoutubeVideo createVideo(YoutubeVideo youtubeVideo) {
        return youtubeVideoRepo.save(youtubeVideo);
    }

    @Override
    public YoutubeVideo updateVideo(YoutubeVideo youtubeVideo, Long id) {
        YoutubeVideo y = youtubeVideoRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Video where id: " + id + " not exist"));
        y.setDescription(youtubeVideo.getDescription());
        y.setStatus(youtubeVideo.isStatus());
        y.setUrl(youtubeVideo.getUrl());
        return youtubeVideoRepo.save(y);
    }

    @Override
    public void deleteVideo(Long id) {
        YoutubeVideo y = youtubeVideoRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Video where id: " + id + " not exist"));
        youtubeVideoRepo.delete(y);
    }

    @Override
    public List<YoutubeVideo> getAllVideoYoutube() {
        return youtubeVideoRepo.findAll();
    }
}
