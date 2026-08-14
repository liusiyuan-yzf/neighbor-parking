package com.neighborparking.config;

import com.neighborparking.domain.AppUser;
import com.neighborparking.domain.AvailabilitySlot;
import com.neighborparking.domain.Community;
import com.neighborparking.domain.ParkingSpace;
import com.neighborparking.domain.Vehicle;
import com.neighborparking.domain.enums.SpaceStatus;
import com.neighborparking.domain.enums.UserRole;
import com.neighborparking.repository.AppUserRepository;
import com.neighborparking.repository.AvailabilitySlotRepository;
import com.neighborparking.repository.CommunityRepository;
import com.neighborparking.repository.ParkingSpaceRepository;
import com.neighborparking.repository.VehicleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.LinkedHashSet;

@Component
@Profile({"local", "demo"})
public class DemoDataInitializer implements CommandLineRunner {

    private final AppUserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final VehicleRepository vehicleRepository;
    private final ParkingSpaceRepository spaceRepository;
    private final AvailabilitySlotRepository slotRepository;

    public DemoDataInitializer(AppUserRepository userRepository, CommunityRepository communityRepository,
                               VehicleRepository vehicleRepository, ParkingSpaceRepository spaceRepository,
                               AvailabilitySlotRepository slotRepository) {
        this.userRepository = userRepository;
        this.communityRepository = communityRepository;
        this.vehicleRepository = vehicleRepository;
        this.spaceRepository = spaceRepository;
        this.slotRepository = slotRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        AppUser renter = userRepository.save(user("小林", "138****0001", UserRole.USER));
        AppUser owner = userRepository.save(user("王阿姨", "138****0002", UserRole.USER, UserRole.OWNER));
        userRepository.save(user("物业管理员", "138****0003", UserRole.PROPERTY_ADMIN, UserRole.PLATFORM_ADMIN));

        Community community = new Community();
        community.setName("阳光花园");
        community.setAddress("上海市浦东新区示范路 88 号");
        community.setLatitude(31.2304D);
        community.setLongitude(121.4737D);
        community = communityRepository.save(community);

        Vehicle vehicle = new Vehicle();
        vehicle.setUserId(renter.getId());
        vehicle.setPlateNumber("沪A·12345");
        vehicle.setVehicleType("小型轿车");
        vehicleRepository.save(vehicle);

        ParkingSpace space = new ParkingSpace();
        space.setOwnerId(owner.getId());
        space.setCommunityId(community.getId());
        space.setSpaceCode("B2-128");
        space.setTitle("近 2 号门地下车位");
        space.setAccessInstructions("从 2 号门进入，向物业出示预约详情后下地库 B2 层。");
        space.setVehicleLimit("小型及紧凑型车辆");
        space.setStatus(SpaceStatus.APPROVED);
        space.setReviewNote("本地演示数据，已通过审核");
        space = spaceRepository.save(space);

        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setSpaceId(space.getId());
        slot.setStartAt(Instant.now().minus(1, ChronoUnit.HOURS));
        slot.setEndAt(Instant.now().plus(7, ChronoUnit.DAYS));
        slotRepository.save(slot);
    }

    private AppUser user(String nickname, String phoneMasked, UserRole... roles) {
        AppUser user = new AppUser();
        user.setNickname(nickname);
        user.setPhoneMasked(phoneMasked);
        user.setRoles(new LinkedHashSet<UserRole>(Arrays.asList(roles)));
        return user;
    }
}
