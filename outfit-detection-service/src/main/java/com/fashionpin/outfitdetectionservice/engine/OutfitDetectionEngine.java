package com.fashionpin.outfitdetectionservice.engine;

import com.fashionpin.outfitdetectionservice.entity.DetectionJob;

public interface OutfitDetectionEngine {
    DetectionJob process(DetectionJob job);
}
