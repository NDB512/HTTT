package com.example.shopping.config;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import com.example.shopping.models.UserDtls;
import com.example.shopping.repositories.UserRepository;
import com.example.shopping.services.UserService;
import com.example.shopping.utils.AppConstant;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component 
public class AuthFailureHandlerImpl extends SimpleUrlAuthenticationFailureHandler{

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {

        String email = request.getParameter("username");
        
        UserDtls userDtls = userRepository.findByEmail(email);

        // Kiểm tra trạng thái tài khoản
        if(userDtls!=null){
            if(userDtls.getIsEnable()){
                if(userDtls.getAccountNonLock()){ //Nếu tài khoản được kích hoạt và chưa bị khóa
                    if(userDtls.getFailedAttempt()<AppConstant.ATTEMPT_TIME){ //Nếu số lần đăng nhập sai dưới giới hạn, tăng số lần thất bại
                        userService.increaseFailedAttempt(userDtls);
                    } else { //Nếu vượt quá giới hạn, khóa tài khoản và thông báo tài khoản bị khóa.
                        userService.userAccountLock(userDtls);
                        exception = new LockedException("Tài khoản của bạn đã bị khóa do nhập sai thông tin đăng nhập quá 3 lần.");
                    }
    
                }else{ //Nếu tài khoản đã bị khóa, kiểm tra thời gian mở khóa đã hết hạn chưa
                    if(userService.unlockAccountTimeExpired(userDtls)){
                        exception = new LockedException("Tài khoản của bạn không bị khóa !! Thử đăng nhập sau");
                    } else {
                        exception = new LockedException("Tài khoản của bạn bị khóa !! Thử lại lần sau");
                    }
                }
    
            } else { //Nếu tài khoản chưa kích hoạt, thông báo
                exception = new LockedException("Tài khoản của bạn bị vô hiệu");
            }
        } else {
            exception = new LockedException("Không đúng, xin nhập lại !");
        }

        request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION", exception);
        response.sendRedirect(request.getContextPath() + "/signin?error");
    }
    
}
