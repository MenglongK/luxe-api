package co.istad.application.seeder;

import co.istad.corefeature.brand.entity.BrandEntity;
import co.istad.corefeature.brand.repository.BrandRepository;
import co.istad.corefeature.category.entity.CategoryEntity;
import co.istad.corefeature.category.repository.CategoryRepository;
import co.istad.corefeature.common.enums.*;
import co.istad.corefeature.coupon.entity.CouponEntity;
import co.istad.corefeature.coupon.repository.CouponRepository;
import co.istad.corefeature.customerpoint.entity.CustomerPointEntity;
import co.istad.corefeature.customerpoint.entity.CustomerPointTransactionEntity;
import co.istad.corefeature.customerpoint.repository.CustomerPointRepository;
import co.istad.corefeature.customerpoint.repository.CustomerPointTransactionRepository;
import co.istad.corefeature.facility.dto.AddressRequest;
import co.istad.corefeature.facility.entity.Address;
import co.istad.corefeature.facility.entity.FacilityEntity;
import co.istad.corefeature.facility.repository.FacilityRepository;
import co.istad.corefeature.file.dto.CustomMultipartFile;
import co.istad.corefeature.file.service.FileStorageService;
import co.istad.corefeature.inventory.facility_inventory.entity.FacilityInventoryEntity;
import co.istad.corefeature.inventory.facility_inventory.repository.FacilityInventoryRepository;
import co.istad.corefeature.order.dto.OrderRequest;
import co.istad.corefeature.order.order_item.dto.OrderItemRequest;
import co.istad.corefeature.order.service.OrderService;
import co.istad.corefeature.product.entity.ProductEntity;
import co.istad.corefeature.product.product_variant.entity.ProductVariantEntity;
import co.istad.corefeature.product.product_variant.repository.ProductVariantRepository;
import co.istad.corefeature.product.repository.ProductRepository;
import co.istad.corefeature.review.dto.ReviewRequest;
import co.istad.corefeature.review.service.ReviewService;
import co.istad.corefeature.supplier.entity.SupplierEntity;
import co.istad.corefeature.supplier.repository.SupplierRepository;
import co.istad.corefeature.user.customer.entity.CustomerEntity;
import co.istad.corefeature.user.customer.repository.CustomerRepository;
import co.istad.corefeature.wallet.entity.WalletEntity;
import co.istad.corefeature.wallet.repository.WalletRepository;
import co.istad.corefeature.wishlist.dto.WishlistItemRequest;
import co.istad.corefeature.wishlist.dto.WishlistRequest;
import co.istad.corefeature.wishlist.dto.WishlistResponse;
import co.istad.corefeature.wishlist.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestDataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final FacilityRepository facilityRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final FacilityInventoryRepository facilityInventoryRepository;
    private final CustomerRepository customerRepository;
    private final CustomerPointRepository customerPointRepository;
    private final CustomerPointTransactionRepository customerPointTransactionRepository;
    private final WalletRepository walletRepository;
    private final OrderService orderService;
    private final ReviewService reviewService;
    private final WishlistService wishlistService;
    private final CouponRepository couponRepository;
    private final FileStorageService fileStorageService;
    private final SupplierRepository supplierRepository;

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            return;
        }

        List<CategoryEntity> categories = seedCategories();
        List<BrandEntity> brands = seedBrands();
        List<FacilityEntity> facilities = seedFacilities();
        List<ProductEntity> products = seedProducts(categories, brands);
        List<ProductVariantEntity> variants = seedVariants(products);
        seedInventories(facilities, variants);
        seedCustomers();
        seedCustomerPoints();
        seedWallets();
        seedOrders(facilities, variants);
        seedReviews(products);
        seedWishlists(variants);
        seedCoupons();
        seedFiles();
        seedSuppliers();
    }

    private List<CategoryEntity> seedCategories() {
        String[] names = {
                "Haute Tailoring", "Cashmere Knitwear", "Leather Goods", "Outerwear",
                "Evening Silk", "Fine Footwear", "High Timepieces", "Fine Jewelry",
                "Artisanal Fragrance", "Bespoke Eyewear"
        };
        List<CategoryEntity> list = new ArrayList<>();
        for (String name : names) {
            CategoryEntity c = new CategoryEntity();
            c.setName(name);
            c.setIsActive(true);
            list.add(categoryRepository.save(c));
        }
        return list;
    }

    private List<BrandEntity> seedBrands() {
        String[] names = {
                "Hermes", "Loro Piana", "Brunello Cucinelli", "Berluti",
                "Tom Ford", "Bottega Veneta", "Saint Laurent", "Celine",
                "The Row", "Ermenegildo Zegna"
        };
        List<BrandEntity> list = new ArrayList<>();
        for (String name : names) {
            BrandEntity b = new BrandEntity();
            b.setName(name);
            b.setLogoUrl("https://images.luxe-atelier.com/brands/" + name.toLowerCase().replace(" ", "-") + ".png");
            b.setIsActive(true);
            list.add(brandRepository.save(b));
        }
        return list;
    }

    private List<FacilityEntity> seedFacilities() {
        String[][] data = {
                {"FAC-BKK1", "Phnom Penh BKK1 Flagship", "No. 128 Norodom Blvd", "Phnom Penh", "Phnom Penh", "12000", "Cambodia"},
                {"FAC-EXSQ", "Exchange Square Boutique", "Street 106 Corner", "Phnom Penh", "Phnom Penh", "12000", "Cambodia"},
                {"FAC-TKAT", "Tuol Kork VIP Atelier", "Street 315", "Phnom Penh", "Phnom Penh", "12000", "Cambodia"},
                {"FAC-SRHG", "Siem Reap Heritage Galleria", "Pokambor Ave", "Siem Reap", "Siem Reap", "17000", "Cambodia"},
                {"FAC-MBS", "Marina Bay Sands Salon", "10 Bayfront Ave", "Singapore", "Central", "018956", "Singapore"},
                {"FAC-CEMB", "Central Embassy Suite", "1031 Ploenchit Rd", "Bangkok", "Pathum Wan", "10330", "Thailand"},
                {"FAC-GNZA", "Ginza Six Atelier", "6 Chome-10-1 Ginza", "Tokyo", "Chuo-ku", "1040061", "Japan"},
                {"FAC-FSH", "Faubourg Saint-Honore", "24 Rue du Faubourg", "Paris", "Ile-de-France", "75008", "France"},
                {"FAC-MNP", "Via Montenapoleone", "Via Monte Napoleone 8", "Milan", "Lombardy", "20121", "Italy"},
                {"FAC-MAYF", "Mayfair Flagship Lounge", "15 Old Bond Street", "London", "Greater London", "W1S 4AX", "United Kingdom"}
        };
        List<FacilityEntity> list = new ArrayList<>();
        for (String[] d : data) {
            Address addr = Address.builder()
                    .addressLine1(d[2])
                    .city(d[3])
                    .province(d[4])
                    .postalCode(d[5])
                    .country(d[6])
                    .build();
            FacilityEntity f = FacilityEntity.builder()
                    .code(d[0])
                    .name(d[1])
                    .type(FacilityType.BRANCH)
                    .address(addr)
                    .isActive(true)
                    .build();
            list.add(facilityRepository.save(f));
        }
        return list;
    }

    private List<ProductEntity> seedProducts(List<CategoryEntity> categories, List<BrandEntity> brands) {
        String[] titles = {
                "Vicuna Double-Breasted Blazer",
                "Heritage 12-Ply Cashmere Turtleneck",
                "Patinated Venezia Leather Briefcase",
                "Waterproof Storm-System Trench Coat",
                "Hand-Dyed Mulberry Silk Evening Gown",
                "Norwegian-Welted Oxford Shoes",
                "Perpetual Calendar Chronograph",
                "Diamond-Paved Heritage Bangle",
                "Oud Imperial Extrait de Parfum",
                "Titanium Aviator Sunglasses"
        };

        List<ProductEntity> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ProductEntity p = ProductEntity.builder()
                    .name(titles[i])
                    .descriptions("Signature handcrafted luxury piece representing artisanal craftsmanship.")
                    .gender(i % 2 == 0 ? Gender.MEN : Gender.WOMEN)
                    .discountType(DiscountType.NONE)
                    .isActive(true)
                    .skuCode("LX-PRD-" + String.format("%03d", i + 1))
                    .categoryId(categories.get(i).getId())
                    .brandId(brands.get(i).getId())
                    .imageUrls(List.of("https://images.luxe-atelier.com/products/prd-" + (i + 1) + ".jpg"))
                    .build();
            list.add(productRepository.save(p));
        }
        return list;
    }

    private List<ProductVariantEntity> seedVariants(List<ProductEntity> products) {
        String[] prices = {
                "4500.00", "1850.00", "3200.00", "2800.00", "3900.00",
                "1450.00", "28500.00", "12000.00", "480.00", "750.00"
        };
        List<ProductVariantEntity> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ProductVariantEntity v = ProductVariantEntity.builder()
                    .product(products.get(i))
                    .price(new BigDecimal(prices[i]))
                    .priceIn(new BigDecimal(prices[i]).multiply(new BigDecimal("0.55")))
                    .attributes(Map.of("size", "Standard", "color", "Signature Luxe"))
                    .isActive(true)
                    .build();
            list.add(productVariantRepository.save(v));
        }
        return list;
    }

    private void seedInventories(List<FacilityEntity> facilities, List<ProductVariantEntity> variants) {
        for (FacilityEntity f : facilities) {
            for (ProductVariantEntity v : variants) {
                FacilityInventoryEntity inv = FacilityInventoryEntity.builder()
                        .facility(f)
                        .productVariant(v)
                        .quantityOnHand(100)
                        .reservedQuantity(0)
                        .isActive(true)
                        .build();
                facilityInventoryRepository.save(inv);
            }
        }
    }

    private void seedCustomers() {
        String[] customerNames = {
                "Lady Genevieve Vance", "Duke Alexander Montclaire", "Princess Sophie Al-Saud",
                "Lord Christian Kensington", "Madame Elena Rostova", "Baron Henrik Lindqvist",
                "Sir Arthur Pendelton", "Viscount William Sterling", "Duchess Catherine Beaumont",
                "Ambassador Menglong Keo"
        };

        CustomerTier[] tiers = {
                CustomerTier.VIP_ROYAL, CustomerTier.VIP_ROYAL, CustomerTier.VIP_ROYAL,
                CustomerTier.GOLD_ATELIER, CustomerTier.GOLD_ATELIER, CustomerTier.GOLD_ATELIER,
                CustomerTier.SILVER, CustomerTier.SILVER, CustomerTier.SILVER,
                CustomerTier.VIP_ROYAL
        };

        Gender[] genders = {
                Gender.WOMEN, Gender.MEN, Gender.WOMEN,
                Gender.MEN, Gender.WOMEN, Gender.MEN,
                Gender.MEN, Gender.MEN, Gender.WOMEN,
                Gender.MEN
        };

        String[] bespokeNotes = {
                "Prefers Vicuna and 12-ply cashmere; private styling salon appointments only.",
                "Bespoke tailoring client; bespoke shoulder measurements and monogram on file.",
                "High jewelry and evening silk gowns; requests discrete white-glove courier delivery.",
                "Collector of perpetual calendar timepieces and patinated Venezia leather luggage.",
                "Regular guest at seasonal Parisian haute couture previews and private viewings.",
                "Favors storm-system outerwear and hand-welted Norwegian leather footwear.",
                "Patron of heritage silk ties, fine tailoring, and bespoke aviator sunglasses.",
                "Frequent international business traveller; requests wrinkle-resistant travel suiting.",
                "Fine jewelry connoisseur; prefers diamond-paved pieces and Cambodi oud extraits.",
                "Diplomatic protocol styling; priority concierge checkout with Bakong KHQR settlement."
        };

        String[] avatars = {
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80",
                "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=400&q=80",
                "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80",
                "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=400&q=80",
                "https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=400&q=80",
                "https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?auto=format&fit=crop&w=400&q=80",
                "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?auto=format&fit=crop&w=400&q=80",
                "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=400&q=80",
                "https://images.unsplash.com/photo-1522075469751-3a6694fb2f61?auto=format&fit=crop&w=400&q=80"
        };

        for (int i = 0; i < 10; i++) {
            Address address = Address.builder()
                    .addressLine1("No. " + (100 + i) + " Luxury Boulevard")
                    .city("Phnom Penh")
                    .province("Phnom Penh")
                    .postalCode("12000")
                    .country("Cambodia")
                    .build();

            CustomerEntity customer = CustomerEntity.builder()
                    .customerCode("cust-vip-" + String.format("%03d", i + 1))
                    .name(customerNames[i])
                    .email("vip" + (i + 1) + "@luxe-atelier.com")
                    .phone("+855 12 " + (100000 + i * 11111))
                    .avatarUrl(avatars[i])
                    .tier(tiers[i])
                    .gender(genders[i])
                    .primaryAddress(address)
                    .bespokeNotes(bespokeNotes[i])
                    .isActive(true)
                    .build();

            customerRepository.save(customer);
        }
    }

    private void seedCustomerPoints() {
        for (int i = 1; i <= 10; i++) {
            String customerCode = "cust-vip-" + String.format("%03d", i);
            int points = 1000 * i;
            CustomerPointEntity cp = CustomerPointEntity.builder()
                    .customerId(customerCode)
                    .totalPoints(points)
                    .isActive(true)
                    .build();
            cp = customerPointRepository.save(cp);

            CustomerPointTransactionEntity cpt = CustomerPointTransactionEntity.builder()
                    .customerPointId(cp.getId())
                    .customerId(customerCode)
                    .transactionType(PointTransactionType.EARNED)
                    .points(points)
                    .balanceAfter(points)
                    .referenceId("INIT-VIP-" + i)
                    .description("VIP Welcome and Atelier tier initialization points")
                    .build();
            customerPointTransactionRepository.save(cpt);
        }
    }

    private void seedWallets() {
        for (int i = 1; i <= 10; i++) {
            String customerCode = "cust-vip-" + String.format("%03d", i);
            BigDecimal balance = new BigDecimal(500 * i + ".00");
            WalletEntity wallet = WalletEntity.builder()
                    .customerId(customerCode)
                    .walletNumber("WAL-LUXE-" + String.format("%04d", i))
                    .balance(balance)
                    .currency("USD")
                    .status(WalletStatus.ACTIVE)
                    .isActive(true)
                    .build();
            walletRepository.save(wallet);
        }
    }

    private void seedOrders(List<FacilityEntity> facilities, List<ProductVariantEntity> variants) {
        String[] customerNames = {
                "Lady Genevieve Vance", "Duke Alexander Montclaire", "Princess Sophie Al-Saud",
                "Lord Christian Kensington", "Madame Elena Rostova", "Baron Henrik Lindqvist",
                "Sir Arthur Pendelton", "Viscount William Sterling", "Duchess Catherine Beaumont",
                "Ambassador Menglong Keo"
        };

        FacilityEntity mainFacility = facilities.get(0);

        for (int i = 0; i < 10; i++) {
            ProductVariantEntity v = variants.get(i);
            AddressRequest address = AddressRequest.builder()
                    .addressLine1("No. " + (100 + i) + " Luxury Boulevard")
                    .city("Phnom Penh")
                    .province("Phnom Penh")
                    .postalCode("12000")
                    .country("Cambodia")
                    .build();

            OrderRequest req = OrderRequest.builder()
                    .customerId("cust-vip-" + String.format("%03d", i + 1))
                    .customerName(customerNames[i])
                    .customerEmail("vip" + (i + 1) + "@luxe-atelier.com")
                    .customerPhone("+855 12 " + (100000 + i * 11111))
                    .facilityId(mainFacility.getId())
                    .orderType(OrderType.ONLINE)
                    .paymentMethod(i % 2 == 0 ? PaymentMethod.BAKONG_KHQR : PaymentMethod.CREDIT_CARD)
                    .shippingAddress(address)
                    .shippingFee(BigDecimal.valueOf(25))
                    .discountAmount(BigDecimal.valueOf(50))
                    .items(List.of(new OrderItemRequest(v.getId(), 1)))
                    .build();

            orderService.create(req);
        }
    }

    private void seedReviews(List<ProductEntity> products) {
        String[] authors = {
                "Lady Genevieve Vance", "Duke Alexander Montclaire", "Princess Sophie Al-Saud",
                "Lord Christian Kensington", "Madame Elena Rostova", "Baron Henrik Lindqvist",
                "Sir Arthur Pendelton", "Viscount William Sterling", "Duchess Catherine Beaumont",
                "Ambassador Menglong Keo"
        };

        String[] titles = {
                "Unmatched drape and vicuna quality",
                "Finest cashmere in the world",
                "Exquisite hand-patinated leather",
                "Superb weather protection and fit",
                "Breathtaking evening silk movement",
                "Robust yet refined oxford construction",
                "Masterpiece of haute horlogerie",
                "Flawless stone clarity and clasp",
                "Sensual Cambodi agarwood sillage",
                "Weightless titanium comfort"
        };

        String[] comments = {
                "The vicuna fiber quality is breathtaking and drape is impeccable.",
                "Extraordinarily warm yet remarkably lightweight.",
                "The burnished patina develops such rich depth over time.",
                "Storm-system wool performs flawlessly in rainy London.",
                "Drapes like liquid silk under evening lights. Truly couture.",
                "Substantial welt construction and comfortable after short break-in.",
                "Grand feu enamel dial and finishing are museum-grade.",
                "Subtle elegance with exceptional stone clarity and secure lock.",
                "Pure Cambodi oud harmonized with May rose. Lasts all evening.",
                "Superb polarized optics and weightless comfort on the bridge."
        };

        FitFeedback[] fits = {
                FitFeedback.TRUE_TO_SIZE, FitFeedback.TRUE_TO_SIZE, FitFeedback.TRUE_TO_SIZE,
                FitFeedback.RUNS_SLIGHTLY_LARGE, FitFeedback.TRUE_TO_SIZE, FitFeedback.RUNS_SLIGHTLY_SMALL,
                FitFeedback.TRUE_TO_SIZE, FitFeedback.TRUE_TO_SIZE, FitFeedback.TRUE_TO_SIZE,
                FitFeedback.TRUE_TO_SIZE
        };

        String[] sizes = {"50R", "M", "Standard", "48L", "38", "42 EU", "41mm", "17cm", "100ml", "Standard"};
        String[] colors = {"Charcoal", "Oatmeal", "Tobacco", "Navy", "Emerald", "Burgundy", "Enamel Blue", "White Gold", "Amber", "Palladium"};

        for (int i = 0; i < 10; i++) {
            ReviewRequest req = ReviewRequest.builder()
                    .productId(products.get(i).getId())
                    .customerId("cust-vip-" + String.format("%03d", i + 1))
                    .authorName(authors[i])
                    .rating(i == 5 ? 4 : 5)
                    .title(titles[i])
                    .comment(comments[i])
                    .fitFeedback(fits[i])
                    .sizePurchased(sizes[i])
                    .colorPurchased(colors[i])
                    .build();

            reviewService.create(req);
        }
    }

    private void seedWishlists(List<ProductVariantEntity> variants) {
        String[] wishlistNames = {
                "Genevieve's Winter Salon Curation",
                "Alexander's Bespoke Suiting Selections",
                "Royal Gala & Haute Joaillerie",
                "Kensington Horology & Venezia Leather",
                "Parisian Runway Evening Edit",
                "Nordic Alpine Outerwear Edit",
                "Oxford Formalwear & Heritage Accessories",
                "Mayfair Executive Travel Wardrobe",
                "Duchess High Jewelry & Fragrance Picks",
                "Diplomatic Mission VIP Protocol Edit"
        };

        String[] itemNotes = {
                "Saved for autumn atelier salon viewing",
                "Awaiting custom shoulder monogram consultation",
                "Planned for Monaco gala reception",
                "Matching Venezia briefcase accessory",
                "To pair with mulberry silk gown",
                "Waterproof storm-system protection",
                "Bespoke polarized titanium sunglasses",
                "Wrinkle-resistant luxury travel fabric",
                "Cambodi oud extrait reserve pairing",
                "Official delegation priority reservation"
        };

        for (int i = 0; i < 10; i++) {
            String customerId = "cust-vip-" + String.format("%03d", i + 1);

            WishlistRequest req = WishlistRequest.builder()
                    .customerId(customerId)
                    .name(wishlistNames[i])
                    .build();

            WishlistResponse wishlist = wishlistService.create(req);

            ProductVariantEntity v1 = variants.get((i + 1) % variants.size());
            wishlistService.addItem(wishlist.id(), new WishlistItemRequest(v1.getId(), itemNotes[i]));

            ProductVariantEntity v2 = variants.get((i + 3) % variants.size());
            wishlistService.addItem(wishlist.id(), new WishlistItemRequest(v2.getId(), "Private concierge reservation request"));
        }
    }

    private void seedCoupons() {
        Object[][] couponData = {
                {"ATELIER15", "Welcome 15% discount for verified Atelier Society members", DiscountType.PERCENTAGE, new BigDecimal("15.00"), new BigDecimal("300.00"), new BigDecimal("500.00"), 500, 142},
                {"VIPROYAL200", "$200 gift voucher for VIP Royal clients spending over $1,500", DiscountType.FIXED_AMOUNT, new BigDecimal("200.00"), new BigDecimal("1500.00"), null, 100, 38},
                {"FALLCOLLECTION10", "10% off preview for Autumn/Winter Cashmere & Tailoring", DiscountType.PERCENTAGE, new BigDecimal("10.00"), new BigDecimal("500.00"), new BigDecimal("400.00"), 300, 85},
                {"FIRSTLUXE50", "$50 welcome voucher for first-time atelier purchases", DiscountType.FIXED_AMOUNT, new BigDecimal("50.00"), new BigDecimal("250.00"), null, 1000, 420},
                {"HIGHJEWELRY500", "$500 gift voucher on Haute Joaillerie over $5,000", DiscountType.FIXED_AMOUNT, new BigDecimal("500.00"), new BigDecimal("5000.00"), null, 50, 12},
                {"BESPOKESPRING20", "20% off Spring bespoke tailoring commission", DiscountType.PERCENTAGE, new BigDecimal("20.00"), new BigDecimal("1000.00"), new BigDecimal("800.00"), 150, 47},
                {"BAKONGPRIVILEGE", "5% exclusive rebate when settling via Bakong KHQR", DiscountType.PERCENTAGE, new BigDecimal("5.00"), new BigDecimal("200.00"), new BigDecimal("250.00"), 2000, 610},
                {"MONTECARLOGALA", "$300 invitation voucher for Cannes & Monaco gala patrons", DiscountType.FIXED_AMOUNT, new BigDecimal("300.00"), new BigDecimal("2500.00"), null, 75, 19},
                {"SUMMERSILK12", "12% off Evening Silk & Resort Collections", DiscountType.PERCENTAGE, new BigDecimal("12.00"), new BigDecimal("400.00"), new BigDecimal("350.00"), 400, 114},
                {"HERITAGEVIP100", "$100 loyalty voucher for clients in Tier Silver and above", DiscountType.FIXED_AMOUNT, new BigDecimal("100.00"), new BigDecimal("800.00"), null, 250, 63}
        };

        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate expiryDate = LocalDate.of(2026, 12, 31);

        for (Object[] d : couponData) {
            CouponEntity c = CouponEntity.builder()
                    .code((String) d[0])
                    .description((String) d[1])
                    .discountType((DiscountType) d[2])
                    .discountValue((BigDecimal) d[3])
                    .minSpend((BigDecimal) d[4])
                    .maxDiscount((BigDecimal) d[5])
                    .usageLimit((Integer) d[6])
                    .usageCount((Integer) d[7])
                    .startDate(startDate)
                    .expiryDate(expiryDate)
                    .status(CouponStatus.ACTIVE)
                    .isActive(true)
                    .build();

            couponRepository.save(c);
        }
    }

    private void seedFiles() {
        String[][] sampleAssets = {
                {"hermes-birkin.jpg", "image/jpeg", "Hermes Birkin 30 Togo Leather Lookbook"},
                {"loro-piana-vicuna.jpg", "image/jpeg", "Loro Piana 100% Pure Vicuna Fabric Catalog"},
                {"cucinelli-cashmere.jpg", "image/jpeg", "Brunello Cucinelli 12-Ply Cashmere Knitwear"},
                {"berluti-alessandro.jpg", "image/jpeg", "Berluti Wholecut Oxford Venezia Patina Gallery"},
                {"tomford-smoking.jpg", "image/jpeg", "Tom Ford Shelton Peak-Lapel Silk Tuxedo"},
                {"bottega-cassette.jpg", "image/jpeg", "Bottega Veneta Padded Intreccio Nappa Asset"},
                {"saintlaurent-le-smoking.jpg", "image/jpeg", "Saint Laurent Grain de Poudre Tailoring"},
                {"celine-triomphe.jpg", "image/jpeg", "Celine Triomphe Polished Calfskin Showcase"},
                {"therow-margaux.jpg", "image/jpeg", "The Row Soft Margaux 15 Saddle Leather Lookbook"},
                {"zegna-triple-stitch.jpg", "image/jpeg", "Zegna Triple Stitch Deerskin Luxury Asset"}
        };

        for (String[] asset : sampleAssets) {
            try {
                byte[] content = ("LUXE_ATELIER_MEDIA_ASSET:" + asset[2]).getBytes(StandardCharsets.UTF_8);
                MultipartFile file = new CustomMultipartFile(asset[0], asset[0], asset[1], content);
                fileStorageService.uploadFile(file);
            } catch (Exception ex) {
                log.info("MinIO initial media seeding notice: {}. Continuing bootstrap.", ex.getMessage());
                break;
            }
        }
    }

    private void seedSuppliers() {
        Object[][] supplierData = {
                {"SUP-LP-01", "Lanificio Loro Piana & C.", SupplierCategory.ITALIAN_WOOL, "Matteo Casagrande", "matteo.c@loropiana-b2b.it", "+39 015 7711", "Corso Rolandi 10, Quarona, Italy", 5, 21, PaymentTerms.NET_60},
                {"SUP-GB-02", "Gobi Mongolian Cashmere Mill", SupplierCategory.MONGOLIAN_CASHMERE, "Bayarmaa Altangerel", "bayarmaa@gobicashmere.mn", "+976 11 342 999", "Khan-Uul District, Ulaanbaatar, Mongolia", 5, 28, PaymentTerms.LETTER_OF_CREDIT},
                {"SUP-GD-03", "Conceria Guidi Rosellini", SupplierCategory.LEATHER_TANNERIES, "Ruggero Guidi", "r.guidi@tannery-guidi.it", "+39 0571 489 001", "Via del Brennero, Pescia, Italy", 4, 30, PaymentTerms.NET_30},
                {"SUP-SL-04", "Atelier de Soieries Lyonnaises", SupplierCategory.FRENCH_SILK, "Elise Delacroix", "elise.delacroix@soieries-lyon.fr", "+33 4 78 28 11 00", "Rue Royale, Lyon, France", 5, 18, PaymentTerms.NET_30},
                {"SUP-PK-05", "Luxe Packaging & Gold Foil Ltd", SupplierCategory.PACKAGING_BOXES, "Vincent Laurent", "v.laurent@luxepackaging.eu", "+41 22 819 4400", "Rue du Rhone 42, Geneva, Switzerland", 4, 14, PaymentTerms.IMMEDIATE},
                {"SUP-EZ-06", "Lanificio Ermenegildo Zegna", SupplierCategory.ITALIAN_WOOL, "Gianluca Zegna", "g.zegna@zegna-textiles.it", "+39 015 75911", "Trivero, Biella, Italy", 5, 20, PaymentTerms.NET_60},
                {"SUP-BC-07", "Cariaggi Fine Yarns", SupplierCategory.MONGOLIAN_CASHMERE, "Francesca Cariaggi", "f.cariaggi@cariaggi.it", "+39 0722 79001", "Cagli, Pesaro e Urbino, Italy", 5, 25, PaymentTerms.NET_30},
                {"SUP-TG-08", "Tanneries Haas", SupplierCategory.LEATHER_TANNERIES, "Jean-Christophe Muller", "jc.muller@tanneries-haas.fr", "+33 3 88 49 50 00", "Eichhoffen, Alsace, France", 5, 24, PaymentTerms.NET_60},
                {"SUP-MB-09", "Maison Bucol Silk Weavers", SupplierCategory.FRENCH_SILK, "Camille Moreau", "c.moreau@bucol-silk.fr", "+33 4 72 10 30 00", "Place Bellecour, Lyon, France", 5, 21, PaymentTerms.LETTER_OF_CREDIT},
                {"SUP-BX-10", "Manufacture de Coffrets Genevois", SupplierCategory.PACKAGING_BOXES, "Henri de Saint-Germain", "henri@coffrets-genevois.ch", "+41 22 731 8800", "Quai du Mont-Blanc, Geneva, Switzerland", 5, 12, PaymentTerms.NET_30}
        };

        for (Object[] d : supplierData) {
            SupplierEntity s = SupplierEntity.builder()
                    .code((String) d[0])
                    .name((String) d[1])
                    .category((SupplierCategory) d[2])
                    .contactPerson((String) d[3])
                    .email((String) d[4])
                    .phone((String) d[5])
                    .address((String) d[6])
                    .rating((Integer) d[7])
                    .leadTimeDays((Integer) d[8])
                    .paymentTerms((PaymentTerms) d[9])
                    .status(SupplierStatus.ACTIVE)
                    .isActive(true)
                    .build();

            supplierRepository.save(s);
        }
    }
}
