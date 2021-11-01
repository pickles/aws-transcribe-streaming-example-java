package com.amazonaws.transcribestreaming;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

public class AudioUtil {

  private static final int MIC_SAMPLE_RATE = 16000;
  private static final int MIC_SAMPLE_SIZE_IN_BITS = 16;
  private static final int MIC_CHANNELS = 1;
  private static final boolean MIC_FLAG_SIGNED = true;
  private static final boolean MIC_FLAG_BIGENDIAN = false;

  private static final AudioFormat MIC_FORMAT = new AudioFormat(
        MIC_SAMPLE_RATE,
        MIC_SAMPLE_SIZE_IN_BITS,
        MIC_CHANNELS,
        MIC_FLAG_SIGNED,
        MIC_FLAG_BIGENDIAN);
  
  private static final DataLine.Info MIC_DATALINE_INFO 
    = new DataLine.Info(TargetDataLine.class, MIC_FORMAT);
  
  public static List<Mixer> getAvailableMics() {
    List<Mixer> mics = new ArrayList<>();
    Mixer.Info[] mixerInfo = AudioSystem.getMixerInfo();
    for (Mixer.Info info : mixerInfo) {
      Mixer currentMixer = AudioSystem.getMixer(info);
      if (currentMixer.isLineSupported(MIC_DATALINE_INFO)) {
        System.out.println(info.getName());
        try {
          // TargetDataLine line = (TargetDataLine) currentMixer.getLine(lineInfo);
          currentMixer.getLine(MIC_DATALINE_INFO);
          mics.add(currentMixer);
        } catch (LineUnavailableException e) {
          System.out.println("Line is unavailable: " + info.getName());
        }
      }
    }

    return mics;
  }

  public static InputStream getStreamFromMic(Mixer mic) throws LineUnavailableException{
    TargetDataLine line = (TargetDataLine) mic.getLine(MIC_DATALINE_INFO);
    line.open(MIC_FORMAT);
    line.start();
    return new AudioInputStream(line);
  }
}
