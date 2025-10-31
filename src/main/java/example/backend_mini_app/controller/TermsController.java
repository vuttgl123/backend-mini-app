package example.backend_mini_app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/legal")
public class TermsController {

    @GetMapping("/terms")
    public String terms() {
        return """
            <!doctype html>
            <html lang="vi"><head><meta charset="utf-8"><title>Điều khoản sử dụng</title></head>
            <body style="font-family: system-ui, -apple-system, Segoe UI, Roboto, sans-serif; line-height:1.6; max-width: 800px; margin: 40px auto; padding: 0 16px;">
              <h1>Điều khoản sử dụng</h1>
              <p>Mini App Test 12 phục vụ mục đích trải nghiệm. Chúng tôi chỉ xử lý các dữ liệu cần thiết cho đăng nhập và hiển thị nội dung trong Mini App.</p>
              <h2>Dữ liệu</h2>
              <ul>
                <li>Dữ liệu do người dùng cung cấp khi tương tác trong ứng dụng;</li>
                <li>Các thông tin do Zalo cấp quyền theo sự đồng ý của người dùng (ví dụ số điện thoại nếu người dùng chấp thuận).</li>
              </ul>
              <h2>Bảo mật</h2>
              <p>Chúng tôi không bán, trao đổi dữ liệu cá nhân. Mọi yêu cầu xóa dữ liệu, vui lòng liên hệ email bên dưới.</p>
              <h2>Liên hệ</h2>
              <p>Email: phamtuanvu1401@gmail.com</p>
              <p>Cập nhật: 2025-10-25</p>
            </body></html>
            """;
    }
}
