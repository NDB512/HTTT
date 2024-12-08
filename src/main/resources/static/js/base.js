$(function() {
    var $userRegister = $("#userRegister");

    $userRegister.validate({
        rules: {
            firstName: {
                required: true
            },
            lastName: {
                required: true
            },
            streetNumber: {
                required: true
            },
            ward: {
                required: true
            },
            district: {
                required: true
            },
            city: {
                required: true
            },
            name: {
                required: true
            },
            phoneNumber: {
                required: true,
                pattern: /^0[0-9]{9,11}$/ // Kiểm tra định dạng số điện thoại
            },
            phone: {
                required: true,
                pattern: /^0[0-9]{9,11}$/ // Kiểm tra định dạng số điện thoại
            },
            email: {
                required: true,
                email: true
            },
            password: {
                required: true,
                minlength: 6 // Thay đổi nếu bạn cần độ dài tối thiểu
            },
            confirmPassword: {
                required: true,
                equalTo: "[name='password']" // Kiểm tra xác minh mật khẩu
            }
        },
        messages: {
            firstName: {
                required: 'Họ là bắt buộc !'
            },
            lastName: {
                required: 'Tên là bắt buộc !'
            },
            streetNumber: {
                required: 'Số đường là bắt buộc !'
            },
            ward: {
                required: 'Xã/Phường là bắt buộc !'
            },
            district: {
                required: 'Quận/Huyện là bắt buộc !'
            },
            city: {
                required: 'Tỉnh/Thành phố là bắt buộc !'
            },
            name: {
                required: 'Tên là bắt buộc !'
            },
            phoneNumber: {
                required: 'Số điện thoại là bắt buộc !',
                pattern: 'Số điện thoại phải bắt đầu bằng 0 và có từ 10 đến 12 số.'
            },
            phone: {
                required: 'Số điện thoại là bắt buộc !',
                pattern: 'Số điện thoại phải bắt đầu bằng 0 và có từ 10 đến 12 số.'
            },
            email: {
                required: 'Email là bắt buộc !',
                email: 'Vui lòng nhập địa chỉ email hợp lệ.'
            },
            password: {
                required: 'Mật khẩu là bắt buộc !',
                minlength: 'Mật khẩu phải có ít nhất 6 ký tự.'
            },
            confirmPassword: {
                required: 'Xác nhận mật khẩu là bắt buộc !',
                equalTo: 'Mật khẩu xác nhận không khớp.'
            }
        }
    });
});
