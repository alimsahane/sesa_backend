package com.sesa.medical.utilities.service;

import com.sesa.medical.utilities.entities.GeneralCondition;
import com.sesa.medical.utilities.entities.PrivacyPolicy;
import com.sesa.medical.utilities.entities.SliderDocument;
import com.sesa.medical.utilities.entities.YoutubeVideo;

import java.util.List;

public interface UtilitieService {

    PrivacyPolicy getprivacyPolicy();

    GeneralCondition getGeneralCondition();

    SliderDocument createSlider(SliderDocument sliderDocument);

    List<SliderDocument> getAllSlider();


    void deleteSliderDocument(Long sliderId);

    YoutubeVideo createVideo(YoutubeVideo youtubeVideo);

    YoutubeVideo updateVideo(YoutubeVideo youtubeVideo,Long id);

    void deleteVideo(Long id);

    List<YoutubeVideo>  getAllVideoYoutube();


}
