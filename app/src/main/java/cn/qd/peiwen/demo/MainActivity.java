package cn.qd.peiwen.demo;

import androidx.appcompat.app.AppCompatActivity;
import cn.qd.peiwen.pwvideoplayer.VideoPlayer;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    private VideoPlayer player;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.player = new VideoPlayer(this);
        this.player.init();
        this.player.pause();
        this.player.setLoop(-1);
        this.player.becomeForeground();
        this.player.setPlayObject("assets://dangdang.wav");
        this.player.load("assets://dangdang.wav");
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                this.player.resume();
                break;
            case R.id.button2:
                this.player.pause();
                break;
        }
    }
}
