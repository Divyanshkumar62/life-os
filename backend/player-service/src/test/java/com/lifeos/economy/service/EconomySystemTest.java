package com.lifeos.economy.service;

import com.lifeos.economy.domain.PlayerEconomy;
import com.lifeos.economy.domain.ShopItem;
import com.lifeos.economy.domain.enums.ShopCategory;
import com.lifeos.economy.repository.PlayerEconomyRepository;
import com.lifeos.economy.repository.PurchaseTransactionRepository;
import com.lifeos.economy.repository.ShopItemRepository;
import com.lifeos.player.domain.PlayerIdentity;
import com.lifeos.player.domain.enums.StatusFlagType;
import com.lifeos.player.dto.PlayerStateResponse;
import com.lifeos.player.dto.PlayerStatusFlagDTO;
import com.lifeos.player.service.PlayerStateService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EconomySystemTest {

    @Mock private PlayerEconomyRepository economyRepository;
    @Mock private ShopItemRepository shopRepository;
    @Mock private PurchaseTransactionRepository transactionRepository;
    @Mock private PlayerStateService playerStateService;

    @InjectMocks private EconomyService economyService;
    @InjectMocks private ShopService shopService;

    private UUID playerId;
    private ShopItem potion;

    @BeforeEach
    void setUp() {
        playerId = UUID.randomUUID();
        // Since ShopService injects EconomyService, we need to handle that.
        // But here we are unit testing ShopService which uses EconomyService.
        // Ideally we Mock EconomyService for ShopServiceTest.
        // Let's create a combined test or use a spy?
        // Let's restructure: define mocked EconomyService for ShopService.
    }

    @Test
    void testAddGold() {
        // Setup
        var economy = PlayerEconomy.builder().player(PlayerIdentity.builder().playerId(playerId).build()).goldBalance(BigDecimal.valueOf(100)).totalGoldEarned(BigDecimal.valueOf(100)).build();
        when(economyRepository.findById(playerId)).thenReturn(Optional.of(economy));

        // Execute
        economyService.addGold(playerId, 50, "Quest");

        // Verify
        assertEquals(BigDecimal.valueOf(150), economy.getGoldBalance());
        assertEquals(BigDecimal.valueOf(150), economy.getTotalGoldEarned());
        verify(economyRepository).save(economy);
    }

    @Test
    void testDeductGold_Success() {
        var economy = PlayerEconomy.builder().player(PlayerIdentity.builder().playerId(playerId).build()).goldBalance(BigDecimal.valueOf(100)).totalGoldSpent(BigDecimal.ZERO).build();
        when(economyRepository.findById(playerId)).thenReturn(Optional.of(economy));

        economyService.deductGold(playerId, 40, "Shop");

        assertEquals(BigDecimal.valueOf(60), economy.getGoldBalance());
        assertEquals(BigDecimal.valueOf(40), economy.getTotalGoldSpent());
    }

    @Test
    void testDeductGold_Insufficient() {
        var economy = PlayerEconomy.builder().player(PlayerIdentity.builder().playerId(playerId).build()).goldBalance(BigDecimal.valueOf(10)).build();
        when(economyRepository.findById(playerId)).thenReturn(Optional.of(economy));

        assertThrows(IllegalStateException.class, () ->
            economyService.deductGold(playerId, 20, "Shop")
        );
    }

    // --- Shop Tests ---

    @Test
    void testShopLocked_InPenaltyZone() {
        // Mock Penalty Zone Active
        PlayerStatusFlagDTO penaltyFlag = new PlayerStatusFlagDTO();
        penaltyFlag.setFlag(StatusFlagType.PENALTY_ZONE);
        
        PlayerStateResponse response = PlayerStateResponse.builder()
                .activeFlags(List.of(penaltyFlag))
                .build();
        
        when(playerStateService.getPlayerState(playerId)).thenReturn(response);

        // Execute & Verify
        assertThrows(IllegalStateException.class, () -> 
            shopService.listItems(playerId)
        );
        
        assertThrows(IllegalStateException.class, () -> 
            shopService.purchaseItem(playerId, "POTION_1")
        );
    }

    @Test
    void testPurchase_Success() {
        // Mock No Penalty
        PlayerStateResponse response = PlayerStateResponse.builder().activeFlags(List.of()).build();
        when(playerStateService.getPlayerState(playerId)).thenReturn(response);

        // Mock Item
        potion = ShopItem.builder()
                .itemId(UUID.randomUUID())
                .code("POTION_FOCUS")
                .name("Focus Potion")
                .cost(50)
                .category(ShopCategory.CONSUMABLE)
                .build();
        when(shopRepository.findByCode("POTION_FOCUS")).thenReturn(Optional.of(potion));

        // Mock Economy Service inside ShopService? 
        // We set up ShopService manually to inject the REAL economyService? 
        // Or we Mock it?
        // Ideally unit tests verify interactions. 
        // Let's rebuild ShopService with MOCK EconomyService for this test.
        EconomyService mockEcon = mock(EconomyService.class);
        ShopService shopSvc = new ShopService(shopRepository, mockEcon, transactionRepository, playerStateService);

        shopSvc.purchaseItem(playerId, "POTION_FOCUS");

        verify(mockEcon).deductGold(eq(playerId), eq(50L), anyString());
        verify(transactionRepository).save(any());
    }
}
