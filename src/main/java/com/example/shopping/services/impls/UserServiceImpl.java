package com.example.shopping.services.impls;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.shopping.models.UserDtls;
import com.example.shopping.repositories.UserRepository;
import com.example.shopping.services.UserService;
import com.example.shopping.utils.AppConstant;
import com.example.shopping.utils.CommonUtil;
import com.example.shopping.utils.OtpUtil;

import jakarta.mail.MessagingException;

// Đánh dấu lớp là một dịch vụ (Service), được Spring quản lý
@Service
public class UserServiceImpl implements UserService {

    // Tự động tiêm đối tượng UserRepository để thao tác với cơ sở dữ liệu
    @Autowired
    private UserRepository userRepository;

    // Tự động tiêm đối tượng PasswordEncoder để mã hóa mật khẩu người dùng
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CommonUtil commonUtil;

    // Phương thức lưu thông tin người dùng mới vào cơ sở dữ liệu
    @Override
    public UserDtls saveUser(UserDtls userDtls) {
        // Thiết lập quyền cho người dùng là "ROLE_USER" mặc định
        userDtls.setRole("ROLE_USER");

        // Thiết lập tài khoản trạng thái chưa được kích hoạt
        userDtls.setIsEnable(false); // Tài khoản chưa được kích hoạt

        // Thiết lập tài khoản trạng thái không bị khóa
        userDtls.setAccountNonLock(true);
        userDtls.setFailedAttempt(0);

        // Mã hóa mật khẩu người dùng và cập nhật lại mật khẩu mã hóa
        String encodePassword = passwordEncoder.encode(userDtls.getPassword());
        userDtls.setPassword(encodePassword);

        // Lưu người dùng vào cơ sở dữ liệu và trả về đối tượng đã lưu
        UserDtls saveUser = userRepository.save(userDtls);

        // Tạo mã OTP
        String otp = OtpUtil.generateOtp();
        saveUser.setOtp(otp);  // Lưu OTP vào cơ sở dữ liệu
        saveUser.setOtpExpirationTime(new Date(System.currentTimeMillis() + 10 * 60 * 1000)); // Thời gian hết hạn 10 phút

        userRepository.save(saveUser); // Lưu lại OTP vào cơ sở dữ liệu

        // Gửi OTP qua email cho người dùng
        try {
            commonUtil.sendOtpEmail(userDtls.getEmail(), otp);
        } catch (UnsupportedEncodingException | MessagingException e) {
            e.printStackTrace();
        }

        return saveUser;
    }

    @Override
    public UserDtls verifyOtp(String otp) {
        // Lấy người dùng từ OTP
        UserDtls user = userRepository.findByOtp(otp);
    
        // Kiểm tra xem người dùng có tồn tại và OTP chưa hết hạn
        if (user != null && user.getOtpExpirationTime().after(new Date())) {
            // OTP hợp lệ và chưa hết hạn
            user.setIsEnable(true); // Kích hoạt tài khoản
            user.setOtp(null); // Xóa OTP sau khi xác nhận thành công
            userRepository.save(user); // Lưu cập nhật vào cơ sở dữ liệu
    
            return user;
        }
    
        return null; // Trả về null nếu OTP không hợp lệ hoặc đã hết hạn
    }
     
    @Override
    public UserDtls saveStaff(UserDtls userDtls) {
        // Thiết lập quyền cho nhân viên là "ROLE_STAFF" mặc định
        userDtls.setRole("ROLE_STAFF");

        // Thiết lập tài khoản trạng thái hoạt động
        userDtls.setIsEnable(true);

        // Thiết lập tài khoản trạng thái không bị khóa
        userDtls.setAccountNonLock(true);
        userDtls.setFailedAttempt(0);

        // Mã hóa mật khẩu người dùng và cập nhật lại mật khẩu mã hóa
        String encodePassword = passwordEncoder.encode(userDtls.getPassword());
        userDtls.setPassword(encodePassword);

        // Lưu người dùng vào cơ sở dữ liệu và trả về đối tượng đã lưu
        UserDtls saveUser = userRepository.save(userDtls);

        return saveUser;
    }

    // Phương thức lấy thông tin người dùng từ cơ sở dữ liệu theo email
    @Override
    public UserDtls getUserByEmail(String email) {
        return userRepository.findByEmail(email); // Trả về đối tượng UserDtls tìm được
    }

    // Phương thức lấy thông tin người dùng từ cơ sở dữ liệu theo vai trò(role)
    @Override
    public List<UserDtls> getUsers(String role) {
        return userRepository.findByRole(role);
    }

     // Phương thức cập nhập trạng thái tài khoản
    @Override
    public Boolean updateAccountStatus(int id, Boolean status) {
        
        Optional<UserDtls> findByUser = userRepository.findById(id);
        if(findByUser.isPresent()){
            UserDtls userDtls = findByUser.get();
            userDtls.setIsEnable(status);
            userRepository.save(userDtls);
            return true;
        }

        return false;
    }

    //Tăng số lần đăng nhập thất bại của người dùng    
    @Override
    public void increaseFailedAttempt(UserDtls user) {
        int attempt = user.getFailedAttempt() + 1;
        user.setFailedAttempt(attempt);
        userRepository.save(user);
    }

    // Khóa tài khoản người dùng và đặt thời gian khóa
    @Override
    public void userAccountLock(UserDtls user) {
        user.setAccountNonLock(false);
        user.setLockTime(new Date());
        userRepository.save(user);
    }

    // Kiểm tra xem thời gian mở khóa tài khoản đã hết hạn chưa; nếu có, mở khóa 
    // tài khoản, đặt lại số lần thử về 0, và xóa thời gian khóa
    @Override
    public boolean unlockAccountTimeExpired(UserDtls user) {
        if (user.getLockTime() == null) {
            // Nếu lockTime là null, có thể tài khoản chưa bao giờ bị khóa
            return false;
        }
        
        Long lockTime = user.getLockTime().getTime();
        Long unlockTime = lockTime + AppConstant.UNLOCK_DURATION_TIME;
    
        Long currentTime = System.currentTimeMillis();
    
        if (unlockTime < currentTime) {
            user.setAccountNonLock(true); // Mở khóa tài khoản
            user.setFailedAttempt(0); // Reset số lần thử về 0
            user.setLockTime(null); // Xóa thời gian khóa
            userRepository.save(user); // Lưu thông tin cập nhật
            return true; // Tài khoản đã được mở khóa
        }
        return false;
    }    

    // Thiết lập lại số lần đăng nhập thất bại (failedAttempt) về 0 cho người dùng dựa trên userId
    @Override
    public void resetAttempt(int userId) {
        UserDtls user = userRepository.findById(userId).orElse(null);
        if (user != null) {
            user.setFailedAttempt(0);
            userRepository.save(user);
        }
    }    

    //Cập nhật mã đặt lại mật khẩu (reset token) cho người dùng bằng email
    @Override
    public void updateUserResetToken(String email, String resetToken) {

        UserDtls userByEmail = userRepository.findByEmail(email);
        userByEmail.setResetToken(resetToken);
        userRepository.save(userByEmail);
    }

    //Lấy thông tin người dùng theo mã token
    @Override
    public UserDtls getUserByToken(String token) {
        return userRepository.findByResetToken(token);
    }

    //Cập nhật chi tiết người dùng
    @Override
    public UserDtls updateUser(UserDtls userDtls) {
        return userRepository.save(userDtls);
    }

    @Override
    public UserDtls updateProfile(UserDtls userDtls, MultipartFile img) {
        Optional<UserDtls> optionalUser = userRepository.findById(userDtls.getId());
        
        if (optionalUser.isPresent()) {
            UserDtls dbUser = optionalUser.get();
            
            // Kiểm tra và lưu hình ảnh nếu có
            if (!img.isEmpty()) {
                String uploadDir = "src/main/resources/static/img/profile_img";
                String newImageName = img.getOriginalFilename();
    
                try {
                    // Chỉ xóa ảnh cũ nếu ảnh đó không phải là ảnh mặc định
                    if (dbUser.getProfileImage() != null 
                            && !dbUser.getProfileImage().isEmpty() 
                            && !dbUser.getProfileImage().equals("default_account.jpg")) {
                        Path oldFilePath = Path.of(uploadDir, dbUser.getProfileImage());
                        Files.deleteIfExists(oldFilePath);
                    }
                    
                    // Lưu ảnh mới
                    Path newFilePath = Path.of(uploadDir, newImageName);
                    img.transferTo(newFilePath);
    
                    // Cập nhật tên ảnh mới vào dbUser
                    dbUser.setProfileImage(newImageName);
                } catch (IOException e) {
                    e.printStackTrace();
                    // Xử lý lỗi nếu không thể lưu ảnh mới, có thể log lỗi hoặc thông báo lỗi nếu cần
                }
            }
    
            // Cập nhật các thông tin cá nhân
            dbUser.setName(userDtls.getName());
            dbUser.setPhoneNumber(userDtls.getPhoneNumber());
            // dbUser.setAddress(userDtls.getAddress());
    
            // Lưu thay đổi vào cơ sở dữ liệu
            userRepository.save(dbUser);
            return dbUser;
        }
    
        return null;
    }

    @Override
    public Boolean existsEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
}
