环境：无要求
开发环境：androidstudio

界面：
界面activity_main.xml由一个EidtText，一个Button和一个VideoView组成。
###@id
EditText @+id/ed_rtsp
Button @+id/btn_submit
VideoView @id/myVideoView

后台：
后台只设置了一个Button的点击事件：调用get_token（）
###
函数 get_token（）
参数：无
功能：
1.登录获取token以应用于后面的请求视频转码 
2.判断RTSP服务器IP，如果是" 58.252.72.172 "将替换成" 192.168.2.3 " 
3.判断RTSP地址中是否有“hikvision”，如果有，则将地址中的" 0? "替换成" 1? "
4.获取EditText中的RTSP地址传进函数rtsp_2_m3u8()
###
函数  rtsp_2_m3u8()
参数：String path（RTSP地址）
功能：
1.以RTSP地址请求转码并获取M3U8流视频地址
2.获取到M3U8流视频的同时定时10秒执行keep_contact()，以保持转码线程的存活
3.播放以上获得的M3U8流视频
###
函数 keep_contact()
参数：String id（M3U8视频id）
功能：发送请求以保持转码线程存活，如返回“OK”，则证明线程存活

提示：
1.如点击转码播放时出现视频无法播放的弹窗，请点击确定后再次点击转码播放按钮后正常播放，如无反应，请检查设备的网络状态是否正常
2.如需要修改服务器地址，在MainActivity.java的位置:
    195行
     227行
      290行

      ###
