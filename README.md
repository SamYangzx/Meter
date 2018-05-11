# Meter
整体功能核心点记录：
首页为模式选择界面（第三版添加功能）并具备与传感器端通信能力；
模式A：
1.拍完照片后可以将照片直接发送给电脑；
2.在单位设置界面：设定好单位后，点击开始要将单位发送给传感器和电脑，并向传感器端发送开始指令；点击结束按钮向传感器端发
送停止采集指令。（目前并未把此按钮作为一次测量的结束）
3.测量界面:
    1)测量模式：每次点击确认，向电脑端发送采集到的数据；
    2）标定模式：向传感器端发送测量点的值；
4.返回到拍照界面时：为一次测量的结束，表现为将拍到的照片存储到新文件夹；

模式B:
必须拍照后后面的功能才有效，诸如存储指令等；
1.照片存储：返回至模式选择界面再次进入模式B 拍的照片存储在之前的文件夹内；
    退出应用重新进入，存储在新的文件夹内；
    倘若没有拍照则不要存储中间过程产生的指令；


========================================
储存到新文件夹；modeA每次返回拍照界面算一次结束；模式B点击按钮算一次结束，要存储照片到新文件夹。
向电脑端发送指令：每次都直接向电脑端发送数据；，modeB:最后一次才发送。

待完成功能；
1.选择模式后，再次返回模式选择界面数据不更新；
2.在A模式下图片传输不正常；
3.A模式下返回单位选择后再次进入测量界面，应该是个新的循环；
===================

V3.01.04.0511
1.修复未拍照再次B模式时，将产生的指令保存在之前的文件夹的问题；

V3.01.04.0506
1.把ChooseModeActivity中的单位换行显示；并添加清零模式功能；
2.版本号重新定义为版本+日期；


V3.01.01
modeA, modeB的初始集成版本

2.01.04.0314 此版本是3月14号，修改完照片像素的稳定版本。

V2.01.03.camera
修复小米手机上返回拍照界面后界面黑屏问题。

V2.01.02
1.修改存储文件文件夹为Meter2;
2.结束测量时增加对话框提示；
3.发送指令界面增加删除建；
4.重构拍照界面


V1.12.08
1.修复相关bug。


V1.12.07
1.修改照片存储文件夹。
2.可以选择发送多张照片。


V1.12.06
1.进入到拍照界面时，会自动连接蓝牙和wifi.


V1.12.05
1.修改蓝牙和wifi状态提示等。

v1.12.pre
1.增加拍照完成后，发送照片功能。（头为start,结束符用end。并未使用之前的传输格式。）
2.修改dialog 为统一格式。



v1.09
  1. 加载项做成首尾相连的可滚动圆盘；
  2. 测量单位和采样单位；
  3. 第N次的箭头；
  4. 蓝牙断开连接另外一个设备；
  5. 优化代码。

v1.08
1.修改界面显示，格式为黑底白字。
2.与电脑端应用进行联调。

meter for liyang
