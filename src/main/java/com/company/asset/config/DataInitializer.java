package com.company.asset.config;

import com.company.asset.domain.asset.Asset;
import com.company.asset.domain.asset.AssetCategory;
import com.company.asset.domain.department.Department;
import com.company.asset.domain.policy.RentalPolicy;
import com.company.asset.domain.user.Role;
import com.company.asset.domain.user.User;
import com.company.asset.repository.AssetCategoryRepository;
import com.company.asset.repository.AssetRepository;
import com.company.asset.repository.DepartmentRepository;
import com.company.asset.repository.RentalPolicyRepository;
import com.company.asset.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final PasswordEncoder passwordEncoder;

    private final AssetCategoryRepository assetCategoryRepository;
    private final AssetRepository assetRepository;

    private final RentalPolicyRepository rentalPolicyRepository; // 없으면 필드/로직 제거

    @Override
    @Transactional
    public void run(String... args) {

        // =========================
        // 0) 정책(옵션): 없으면 1행 생성
        // =========================
        if (rentalPolicyRepository != null && rentalPolicyRepository.count() == 0) {
            rentalPolicyRepository.save(RentalPolicy.builder()
                    .maxRentalDays(7)
                    .maxActiveRentalsPerUser(2)
                    .maxExtensions(1)
                    .build());
        }

        // =========================
        // 1) 부서: 없으면 생성
        // =========================
        Department hq = findOrCreateDept("HQ", null);
        Department it = findOrCreateDept("IT", hq);
        Department hr = findOrCreateDept("HR", hq);

        // =========================
        // 2) 유저: 없으면 생성 (pw: 1234)
        // =========================
        User superAdmin = findOrCreateUser("admin@company.com", "슈퍼관리자", Role.SUPER_ADMIN);
        User assetAdmin = findOrCreateUser("asset@company.com", "자산관리자", Role.ASSET_ADMIN);
        User manager = findOrCreateUser("manager@company.com", "승인자(팀장)", Role.MANAGER);
        User employee = findOrCreateUser("user@company.com", "일반사용자", Role.EMPLOYEE);

        // =========================
        // 3) 카테고리: 없으면 생성
        // =========================
        AssetCategory laptop = findOrCreateCategory("Laptop");
        AssetCategory monitor = findOrCreateCategory("Monitor");
        AssetCategory camera = findOrCreateCategory("Camera");

        // =========================
        // 4) 자산: 없으면 샘플 생성
        //    (이미 있으면 중복 방지)
        // =========================
        if (assetRepository.count() == 0) {
            assetRepository.save(new Asset(
                    laptop, "LAP-001", "SN-LAP-001", "MacBook Pro 14",
                    "HQ 3F 창고", LocalDate.of(2024, 3, 10), 3200000L,
                    it, assetAdmin
            ));

            assetRepository.save(new Asset(
                    laptop, "LAP-002", "SN-LAP-002", "ThinkPad X1 Carbon",
                    "HQ 3F 창고", LocalDate.of(2023, 11, 2), 2600000L,
                    it, assetAdmin
            ));

            assetRepository.save(new Asset(
                    monitor, "MON-001", "SN-MON-001", "Dell 27\" Monitor",
                    "HQ 3F 창고", LocalDate.of(2023, 8, 20), 380000L,
                    it, assetAdmin
            ));

            assetRepository.save(new Asset(
                    monitor, "MON-002", "SN-MON-002", "LG UltraWide 34\"",
                    "HQ 3F 창고", LocalDate.of(2024, 1, 15), 790000L,
                    it, assetAdmin
            ));

            assetRepository.save(new Asset(
                    camera, "CAM-001", "SN-CAM-001", "Sony A7C",
                    "HQ 2F 대여함", LocalDate.of(2022, 6, 5), 1900000L,
                    hr, assetAdmin
            ));
        }
    }

    // =========================
    // helpers
    // =========================
    private Department findOrCreateDept(String name, Department parent) {
        // DepartmentRepository에 findByName이 없으면 추가해줘:
        // Optional<Department> findByName(String name);
        Optional<Department> found = departmentRepository.findByName(name);
        if (found.isPresent()) return found.get();

        Department d = Department.builder()
                .name(name)
                .parent(parent)
                .build();
        return departmentRepository.save(d);
    }

    private User findOrCreateUser(String email, String name, Role role) {
        // UserRepository에 findByEmail이 없으면 추가해줘:
        // Optional<User> findByEmail(String email);
        return userRepository.findByEmail(email).orElseGet(() ->
                userRepository.save(User.builder()
                        .email(email)
                        .password(passwordEncoder.encode("1234"))
                        .name(name)
                        .role(role)
                        .build())
        );
    }

    private AssetCategory findOrCreateCategory(String name) {
        return assetCategoryRepository.findByName(name).orElseGet(() ->
                assetCategoryRepository.save(AssetCategory.builder().name(name).build())
        );
    }
}