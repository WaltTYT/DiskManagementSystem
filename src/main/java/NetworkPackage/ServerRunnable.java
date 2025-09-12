package NetworkPackage;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import static NetworkPackage.Server.*;

public class ServerRunnable implements Runnable {//服务器线程类
    private final Socket userSocket;//客户端接口

    public ServerRunnable(Socket clientSocket) {//构造方法
        this.userSocket = clientSocket;
    }

    @Override
    public void run() {//当线程使用时
        try (DataInputStream dis = new DataInputStream(userSocket.getInputStream());//创建数据输入流
             DataOutputStream dos = new DataOutputStream(userSocket.getOutputStream())) {//创建数据输出流

            switch (dis.readInt()) {//根据客户端输入操作码选择
                case SavaUserInformation -> handleServerSaveUserInformation(dis, dos);//0：存储用户信息
                case LoadUserInformation -> handleServerLoadUserInformation(dis, dos);//1：加载用户信息
                case ModifyUserInformation -> handleServerModifyUserInformation(dis, dos);//2：修改用户信息
                case ValidateUserUnique -> handleServerValidateUserUnique(dis, dos);//3：验证用户名唯一性
                case ValidateUserInformation -> handleServerValidateUserInformation(dis, dos);//4：验证用户信息
                case RemoveUserInformation -> handleServerRemoveUserInformation(dis, dos);//5：删除用户信息
                case SaveUserUploadPicture -> handleServerSaveUserUploadPicture(dis, dos);//6：存储用户上传图片
                case LoadUserUploadPicture -> handleServerLoadUserUploadPicture(dis, dos);//7：加载用户上传图片
                case RemoveUserUploadPicture -> handleServerRemoveUserUploadPicture(dis, dos);//8：删除用户上传图片
                case ReceiveUserErrorLog -> handleServerReceiveUserErrorLog(dis);//9：接收用户错误日志
                case ReceiveUserSuggestionFeedback -> handleServerReceiveUserSuggestionFeedback(dis);//10：接收用户建议反馈
                case ReceiveUserSettingState -> handleServerReceiveUserSettingState(dis);//11：接收用户设置状态
                case GenerateVerificationCode -> handleServerGenerateVerificationCode(dis, dos);//12：生成验证码
                case VerifyVerificationCodeState -> handleServerVerifyVerificationCode(dis, dos);//13：校验验证码
                default -> dos.writeBoolean(false);//默认：返回错误
            }
        } catch (Exception e) {
            handleError(e.getMessage());//处理错误日志
            e.printStackTrace();//捕获异常
        }
    }
}
