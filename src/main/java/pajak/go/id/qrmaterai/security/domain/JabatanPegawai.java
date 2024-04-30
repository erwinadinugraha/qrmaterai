package pajak.go.id.qrmaterai.security.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class JabatanPegawai {
    private String jabatanPegawai_iri;
    private String jabatan_name;
    private String kantor_name;
    private String unit_name;
    private String tipeJabatan_name;
    private List<String> roles = new ArrayList<>();
}
