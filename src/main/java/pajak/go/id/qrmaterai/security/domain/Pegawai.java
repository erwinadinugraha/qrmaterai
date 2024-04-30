package pajak.go.id.qrmaterai.security.domain;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Pegawai {

    private String pegawaiId;
    private String nama;
    private String nip9;
    private String nip18;
    private boolean pensiun;
    private String pangkat;
    private boolean onLeave;
    private List<JabatanPegawai> jabatanPegawais = new ArrayList<>();
}
