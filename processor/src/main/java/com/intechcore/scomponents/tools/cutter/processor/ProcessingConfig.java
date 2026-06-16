package com.intechcore.scomponents.tools.cutter.processor;

import com.intechcore.scomponents.tools.cutter.annotations.common.BoolForce;
import com.intechcore.scomponents.tools.cutter.annotations.CutCodeProcessConfig;

class ProcessingConfig {
    public boolean logProcessing;
    public boolean returnThisIfFound;

    private CutCodeProcessConfig localConfig;

    public ProcessingConfig() {
        this.logProcessing = true;
        this.returnThisIfFound = true;
    }

    public void setLocalProcessingConfig(CutCodeProcessConfig localConfig) {
        this.localConfig = localConfig;
    }

    public boolean logProcessing() {
        return this.getLocalizedValue(
                this.logProcessing,
                this.localConfig == null ? null : this.localConfig.logProcessing()
        );
    }

    public boolean returnThisIfFound() {
        return this.getLocalizedValue(
                this.returnThisIfFound,
                this.localConfig == null ? null : this.localConfig.returnThisIfFound()
        );
    }

    private boolean getLocalizedValue(boolean target, BoolForce local) {
        if (local == null || local == BoolForce.NONE) {
            return target;
        }
        return local == BoolForce.FORCE_TRUE;
    }
}
