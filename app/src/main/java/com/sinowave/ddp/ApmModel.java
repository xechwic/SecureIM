package com.sinowave.ddp;

import android.content.Context;

/**
 * Created by sino on 2016-05-30.
 */
public class ApmModel {

    private Context mContext;
    public ApmModel(Context context){ this.mContext = context;}


    private String targetIP = "127.0.0.1";

    public String getTargetIP(){
        return targetIP;
    }
    public void setTargetIP(String targetIP){
        this.targetIP = targetIP;
    }

    private boolean highPassFilter = true;

    public boolean getHighPassFilter(){
        return highPassFilter;
    }
    public void setHighPassFilter(boolean highPassFilter){
        this.highPassFilter = highPassFilter;
    }


    private boolean speechIntelligibilityEnhance = false;

    public boolean getSpeechIntelligibilityEnhance(){
        return speechIntelligibilityEnhance;
    }
    public void setSpeechIntelligibilityEnhance(boolean speechIntelligibilityEnhance){
        this.speechIntelligibilityEnhance = speechIntelligibilityEnhance;
    }

    private boolean beamForming = false;///true;

    public boolean getBeamForming() {
        return beamForming;
    }
    public void setBeamForming(boolean beamForming){
        this.beamForming = beamForming;
    }

    private boolean aecPC = false;

    public boolean getAecPC(){
        return aecPC;
    }
    public void setAecPC(boolean aecPC){
        this.aecPC = aecPC;
    }

    private boolean aecMobile = true;
    public boolean getAecMobile(){
        return  aecMobile;
    }
    public void setAecMobile(boolean aecMobile){
        this.aecMobile = aecMobile;
    }

    private boolean aecNone = false;
    public boolean getAecNone(){
        return aecNone;
    }
    public void setAecNone(boolean aecNone){
        this.aecNone = aecNone;
    }

    private boolean aecExtendFilter = false;
    public boolean getAecExtendFilter(){
        return aecExtendFilter;
    }
    public void setAecExtendFilter(boolean aecExtendFilter){
        this.aecExtendFilter = aecExtendFilter;
    }

    private boolean delayAgnostic = false;
    public boolean getDelayAgnostic(){
        return delayAgnostic;
    }
    public void setDelayAgnostic(boolean delayAgnostic){
        this.delayAgnostic = delayAgnostic;
    }

    private boolean nextGenerationAEC = false;
    public boolean getNextGenerationAEC(){
        return nextGenerationAEC;
    }
    public void setNextGenerationAEC(boolean nextGenerationAEC){
        this.nextGenerationAEC = nextGenerationAEC;
    }

    private String bufferDelay = "150";
    public String getBufferDelay() {
        return bufferDelay;
    }
    public void setBufferDelay(String bufferDelay){
        this.bufferDelay = bufferDelay;
    }
    public short getBufferDelayMs(){
        try{
            return (short) Integer.parseInt(bufferDelay);
        }catch (Exception e){
            return 150;
        }
    }


    private boolean aecPCMode0 = false;
    private boolean aecPCMode1 = false;
    private boolean aecPCMode2 = true;
    public boolean getAecPCMode0(){
        return aecPCMode0;
    }
    public void setAecPCMode0(boolean aecMode){
        this.aecPCMode0 = aecMode;
    }


    public boolean getAecPCMode1(){
        return aecPCMode1;
    }
    public void setAecPCMode1(boolean aecMode){
        this.aecPCMode1 = aecMode;
    }


    public boolean getAecPCMode2(){
        return aecPCMode2;
    }
    public void setAecPCMode2(boolean aecMode){
        this.aecPCMode2 = aecMode;
    }

    private boolean aecMobileMode0 = false;
    private boolean aecMobileMode1 = false;
    private boolean aecMobileMode2 = false;
    private boolean aecMobileMode3 = true;
    private boolean aecMobileMode4 = false;

    public boolean getAecMobileMode0(){
        return aecPCMode0;
    }
    public void setAecMobileMode0(boolean aecMode){
        this.aecMobileMode0 = aecMode;
    }


    public boolean getAecMobileMode1(){
        return aecMobileMode1;
    }
    public void setAecMobileMode1(boolean aecMode){
        this.aecMobileMode1 = aecMode;
    }


    public boolean getAecMobileMode2(){
        return aecMobileMode2;
    }
    public void setAecMobileMode2(boolean aecMode){
        this.aecMobileMode2 = aecMode;
    }


    public boolean getAecMobileMode3(){
        return aecMobileMode3;
    }
    public void setAecMobileMode3(boolean aecMode){
        this.aecMobileMode3 = aecMode;
    }


    public boolean getAecMobileMode4(){
        return aecMobileMode4;
    }
    public void setAecMobileMode4(boolean aecMode){
        this.aecMobileMode4 = aecMode;
    }


    private boolean ns = true;

    public boolean getNs(){
        return ns;
    }
    public void setNs(boolean ns){
        this.ns = ns;
    }

    private boolean experimentalNS = false;
    public boolean getExperimentalNS(){
        return experimentalNS;
    }
    public void setExperimentalNS(boolean experimentalNS){
        this.experimentalNS = experimentalNS;
    }


    private boolean nsMode0 = false;
    private boolean nsMode1 = false;
    private boolean nsMode2 = true;
    private boolean nsMode3 = false;

    public boolean getNsMode0(){
        return nsMode0;
    }
    public void setNsMode0(boolean nsMode0){
        this.nsMode0 = nsMode0;
    }


    public boolean getNsMode1(){
        return nsMode1;
    }
    public void setNsMode1(boolean nsMode1){
        this.nsMode1 = nsMode1;
    }


    public boolean getNsMode2(){
        return nsMode2;
    }
    public void setNsMode2(boolean nsMode2){
        this.nsMode2 = nsMode2;
    }


    public boolean getNsMode3(){
        return nsMode3;
    }
    public void setNsMode3(boolean nsMode3){
        this.nsMode3 = nsMode3;
    }


    private boolean agc = true;

    public boolean getAgc(){
        return agc;
    }
    public void setAgc(boolean agc){
        this.agc = agc;
    }


    private boolean experimentalAGC = false;

    public boolean getExperimentalAGC(){
        return experimentalAGC;
    }
    public void setExperimentalAGC(boolean experimentalAGC){
        this.experimentalAGC = experimentalAGC;
        //notifyPropertyChanged(BR.experimentalAGC);
    }

    private boolean agcMode0 = true;
    private boolean agcMode1 = false;
    private boolean agcMode2 = false;

    public boolean getAgcMode1(){
        return agcMode1;
    }
    public void setAgcMode1(boolean agcMode1){
        this.agcMode1 = agcMode1;
        //notifyPropertyChanged(BR.agcMode1);
    }


    public boolean getAgcMode2(){
        return agcMode2;
    }
    public void setAgcMode2(boolean agcMode2){
        this.agcMode2 = agcMode2;
        //notifyPropertyChanged(BR.agcMode2);
    }


    public boolean getAgcMode0(){
        return agcMode0;
    }
    public void setAgcMode0(boolean agcMode0){
        this.agcMode0 = agcMode0;
    }


    private String targetLevel = "6";

    public String getTargetLevel(){
        return targetLevel;
    }
    public void setTargetLevel(String level){
        try{
            targetLevelInt = (short) Integer.parseInt(level);
            if(targetLevelInt > 31) targetLevelInt = 31;
            if(targetLevelInt < 0) targetLevelInt = 0;
            this.targetLevel = targetLevelInt + "";
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    ///
    private int targetLevelInt = 30;
    public int getTargetLevelInt(){
        return targetLevelInt;
    }


    private String compressionGain = "9";

    public String getCompressionGain(){return compressionGain;}
    public void setCompressionGain(String gain){
        try{
            compressionGainInt = (short) Integer.parseInt(gain);
            if(compressionGainInt > 90) compressionGainInt = 90;
            if(compressionGainInt < 0) compressionGainInt = 0;
            this.compressionGain = compressionGainInt + "";

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private int compressionGainInt = 9;
    public int getCompressionGainInt(){
        return compressionGainInt;
    }


    ////////////不用webrtc静音检测
    private boolean vad = true;

    public boolean getVad(){
        return vad;
    }

    public void setVad(boolean vad){
        this.vad = vad;
    }


    private int rcvCount = 0;

    public String getRcvCount(){
        return rcvCount+"";
    }
    public void setRcvCount(int rcvCount){
        this.rcvCount = rcvCount;
    }

    private int sndCount = 0;

    public String getSndCount() {
        return sndCount+"";
    }
    public void setSndCount(int count){
        this.sndCount = count;

    }


    private boolean start = false;

    public boolean getStart(){
        return start;
    }



    public void setStart(boolean start){
        this.start = start;
    }

}
