package com.example.shopping.services;

import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.example.shopping.models.UserDtls;

public interface UserService {

    public UserDtls saveUser(UserDtls userDtls) throws UnsupportedEncodingException, Exception;

    public UserDtls getUserByEmail(String email);

    public List<UserDtls> getUsers(String role);

    public Boolean updateAccountStatus(int id, Boolean status);

    public void increaseFailedAttempt(UserDtls user);

    public void userAccountLock(UserDtls user);

    public boolean unlockAccountTimeExpired(UserDtls user);

    public void resetAttempt(int userId);

    public void updateUserResetToken(String email, String resetToken);

    public UserDtls getUserByToken(String token);

    public UserDtls updateUser(UserDtls userDtls);

    public UserDtls updateProfile(UserDtls userDtls,  MultipartFile img);

    public UserDtls saveStaff(UserDtls userDtls);

    public Boolean existsEmail(String email);

    public UserDtls verifyOtp(String otp);
}
