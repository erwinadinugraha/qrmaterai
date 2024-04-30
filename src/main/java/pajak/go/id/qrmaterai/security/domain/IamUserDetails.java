package pajak.go.id.qrmaterai.security.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class IamUserDetails {
    private String id;
    private String username;
    private String userIdentifier;
    private List<String> roles = new ArrayList<>();
    private Integer expiredTime;
    private Pegawai pegawai;


}
