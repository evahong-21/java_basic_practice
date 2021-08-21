package ch07;

class MyTv2 {
  boolean isPowerOn;
  int channel;
  int volume;
  final int MAX_VOLUME = 100;
  final int MIN_VOLUME = 0;
  final int MAX_CHANNEL = 100;
  final int MIN_CHANNEL = 1;

  public int getChannel() {
    return channel;
  }
  public void setChannel(int channel) {
    if (channel>MAX_CHANNEL || channel<MIN_CHANNEL) {
      return;
    }
    this.channel = channel;
  }
  public int getVolume() {
    return volume;
  }
  public void setVolume(int volume) {
    if (channel>MAX_VOLUME || channel<MIN_VOLUME) {
      return;
    }
    this.volume = volume;
  }
}

public class q0710 {
  public static void main(String args[]) {
    MyTv2 t = new MyTv2();
    t.setChannel(10);
    System.out.println("CH:"+t.getChannel());
    t.setVolume(20);
    System.out.println("VOL:"+t.getVolume());
  }
}
