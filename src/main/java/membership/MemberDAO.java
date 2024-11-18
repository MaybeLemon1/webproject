package membership;

import common.JDBConnect;
import jakarta.servlet.ServletContext;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MemberDAO extends JDBConnect {

    // 생성자 1 : 드라이버, 커넥션 URL 등 4개의 매개변수로 정의
    public MemberDAO(String drv, String url, String id, String pw) {
        super(drv, url, id, pw);
    }

    // 생성자 2 : application 내장 객체를 매개변수로 정의
    public MemberDAO(ServletContext application) {
        super(application);
    }

    // 로그인 검증을 위한 메서드
    public MemberDTO getMemberDTO(String uid, String upwd) {
        MemberDTO dto = new MemberDTO();
        String query = "SELECT * FROM member WHERE user_id=? AND user_pwd=?";

        try {
            psmt = con.prepareStatement(query);
            psmt.setString(1, uid);
            psmt.setString(2, upwd);  // 비밀번호를 평문으로 처리
            rs = psmt.executeQuery();

            if (rs.next()) {
                dto.setId(rs.getString("user_id"));
                dto.setPwd(rs.getString("user_pwd"));
                dto.setName(rs.getString("user_name"));
                dto.setEmail(rs.getString("user_email"));
                dto.setPhone(rs.getString("user_phone"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dto;
    }

    // 회원가입을 위한 메서드
    public int insertMember(MemberDTO dto) {
        int result = 0;
        String query = "INSERT INTO member (user_id, user_pwd, user_name, user_email, user_phone) " +
                       "VALUES (?, ?, ?, ?, ?)";

        try {
            psmt = con.prepareStatement(query);
            psmt.setString(1, dto.getId());
            psmt.setString(2, encryptPassword(dto.getPwd())); // 비밀번호 암호화
            psmt.setString(3, dto.getName());
            psmt.setString(4, dto.getEmail());
            psmt.setString(5, dto.getPhone());

            result = psmt.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    // 아이디 중복 체크
    public boolean checkIdExists(String userId) {
        boolean exists = false;
        String query = "SELECT COUNT(*) FROM member WHERE user_id=?";

        try {
            psmt = con.prepareStatement(query);
            psmt.setString(1, userId);
            rs = psmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                exists = (count > 0);  // 아이디가 존재하면 true
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return exists;
    }

    // 비밀번호 암호화 (예시: SHA-256)
    private String encryptPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(Integer.toHexString(0xff & b));
            }
            return hexString.toString();  // 암호화된 비밀번호 반환
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}