package com.fashionpin.productservice.config;

import com.fashionpin.productservice.entity.Category;
import com.fashionpin.productservice.entity.Collection;
import com.fashionpin.productservice.entity.CollectionProduct;
import com.fashionpin.productservice.entity.ItemType;
import com.fashionpin.productservice.entity.Product;
import com.fashionpin.productservice.entity.ProductVariant;
import com.fashionpin.productservice.repository.CategoryRepository;
import com.fashionpin.productservice.repository.CollectionProductRepository;
import com.fashionpin.productservice.repository.CollectionRepository;
import com.fashionpin.productservice.repository.ItemTypeRepository;
import com.fashionpin.productservice.repository.ProductRepository;
import com.fashionpin.productservice.repository.ProductVariantRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Order(10)
@RequiredArgsConstructor
public class StorefrontDataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ItemTypeRepository itemTypeRepository;
    private final CollectionRepository collectionRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CollectionProductRepository collectionProductRepository;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking Storefront Seed Data status...");

        try {
            seedCategoriesAndItemTypes();
            seedStyleCollections();
            seedProductsAndVariants();
            log.info("Storefront Seed Data successfully verified/initialized! Total Products: {}", productRepository.count());
        } catch (Exception e) {
            log.error("Error during Storefront Data Seeding: {}", e.getMessage(), e);
        }
    }

    private void seedCategoriesAndItemTypes() {
        // 1. Men
        Category men = categoryRepository.findBySlug("men").orElseGet(() -> {
            Category c = Category.builder()
                    .id("cat_men")
                    .name("Men's Fashion")
                    .slug("men")
                    .description("Sharp tailoring, structured silhouettes, and contemporary essentials.")
                    .bannerMediaUrl("https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=1400&q=80")
                    .displayOrder(1)
                    .isActive(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            return categoryRepository.save(c);
        });
        men.setName("Men's Fashion");
        men.setBannerMediaUrl("https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=1400&q=80");
        categoryRepository.save(men);

        // 2. Women
        Category women = categoryRepository.findBySlug("women").orElseGet(() -> {
            Category c = Category.builder()
                    .id("cat_women")
                    .name("Women's Fashion")
                    .slug("women")
                    .description("Effortless elegance, sculptural tailoring, and fluid silk pieces.")
                    .bannerMediaUrl("https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=1400&q=80")
                    .displayOrder(2)
                    .isActive(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            return categoryRepository.save(c);
        });
        women.setName("Women's Fashion");
        women.setBannerMediaUrl("https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=1400&q=80");
        categoryRepository.save(women);

        // 3. Kids
        Category kids = categoryRepository.findBySlug("kids").orElseGet(() -> {
            Category c = Category.builder()
                    .id("cat_kids")
                    .name("Kids' Fashion")
                    .slug("kids")
                    .description("Chic mini silhouettes crafted with natural fibers and playful comfort.")
                    .bannerMediaUrl("https://images.unsplash.com/photo-1514090458221-65bb69cf63e6?w=1400&q=80")
                    .displayOrder(3)
                    .isActive(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            return categoryRepository.save(c);
        });
        kids.setName("Kids' Fashion");
        kids.setBannerMediaUrl("https://images.unsplash.com/photo-1514090458221-65bb69cf63e6?w=1400&q=80");
        categoryRepository.save(kids);

        // Item types for Men
        seedItemType(men, "Shoes & Loafers", "shoes", 1);
        seedItemType(men, "Wide-Leg Trousers", "wide-leg-trousers", 2);
        seedItemType(men, "Close-Leg Trousers", "close-leg-trousers", 3);
        seedItemType(men, "Shirts & Overshirts", "shirts", 4);
        seedItemType(men, "T-Shirts & Polos", "t-shirts", 5);
        seedItemType(men, "Tailored Pants & Chinos", "pants", 6);
        seedItemType(men, "Blazers & Overcoats", "outerwear", 7);
        seedItemType(men, "Knitwear & Sweaters", "knitwear", 8);

        // Item types for Women
        seedItemType(women, "Dresses & Gowns", "dresses", 1);
        seedItemType(women, "Blazers & Jackets", "blazers", 2);
        seedItemType(women, "Wide-Leg Trousers", "wide-leg-trousers", 3);
        seedItemType(women, "Close-Leg Trousers", "close-leg-trousers", 4);
        seedItemType(women, "Tops & Silk Shirts", "tops", 5);
        seedItemType(women, "Heels, Boots & Flats", "shoes", 6);
        seedItemType(women, "Skirts", "skirts", 7);
        seedItemType(women, "Bags & Accessories", "bags", 8);

        // Item types for Kids
        seedItemType(kids, "Sets & Co-ords", "sets", 1);
        seedItemType(kids, "Tops, Shirts & Tees", "tops", 2);
        seedItemType(kids, "Trousers & Shorts", "pants", 3);
        seedItemType(kids, "Jackets & Coats", "outerwear", 4);
        seedItemType(kids, "Footwear & Boots", "shoes", 5);
        seedItemType(kids, "Sweaters & Cardigans", "knitwear", 6);
    }

    private void seedItemType(Category cat, String name, String slug, int order) {
        if (!itemTypeRepository.existsByCategoryIdAndSlug(cat.getId(), slug)) {
            ItemType it = ItemType.builder()
                    .category(cat)
                    .name(name)
                    .slug(slug)
                    .displayOrder(order)
                    .isActive(true)
                    .createdAt(Instant.now())
                    .updatedAt(Instant.now())
                    .build();
            itemTypeRepository.save(it);
        }
    }

    private void seedStyleCollections() {
        createOrUpdateCollection(
                "col_old_money",
                "Old Money Aesthetic",
                "old-money",
                "Heritage Tailoring & Aristocratic Elegance",
                "Understated opulence rooted in European estates, Ivy League heritage, crisp linen oxford shirts, cashmere crewnecks, and classic leather horsebit loafers.",
                "https://images.unsplash.com/photo-1519085360753-af0119f7cbe7?w=1400&q=80",
                "#2C3E50",
                1,
                true
        );

        createOrUpdateCollection(
                "col_minimal_luxe",
                "Minimal Luxe",
                "minimal-luxe",
                "Monochromatic Clean Lines & Pure Form",
                "Sharp architectural geometry, pristine black, charcoal, and ivory draping. Elevated essentials stripped of superfluous detail to let premium craft speak.",
                "https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=1400&q=80",
                "#E0D7C6",
                2,
                true
        );

        createOrUpdateCollection(
                "col_street_couture",
                "Street Couture",
                "street-couture",
                "High-Fashion Utilitarian & Heavy Textures",
                "Oversized proportions, technical Japanese nylon, raw heavyweight denim, tactical hardware, and sculptural luxury sneakers.",
                "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=1400&q=80",
                "#D35400",
                3,
                true
        );

        createOrUpdateCollection(
                "col_quiet_luxury",
                "Quiet Luxury",
                "quiet-luxury",
                "Unbranded Excellence & Noble Cashmeres",
                "Impeccable raw cashmere, double-faced vicuña wool, neutral tones, and artisan construction made for discerning connoisseurs.",
                "https://images.unsplash.com/photo-1483985988355-763728e1935b?w=1400&q=80",
                "#A4907C",
                4,
                true
        );

        createOrUpdateCollection(
                "col_dark_academia",
                "Dark Academia",
                "dark-academia",
                "Scholastic Romanticism & Heavy Wools",
                "Pleated high-waisted wool trousers, tweed blazers with elbow patches, cable knits, structured leather brogues, and moody earthy hues.",
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=1400&q=80",
                "#581825",
                5,
                true
        );

        createOrUpdateCollection(
                "col_coastal_chic",
                "Coastal Chic",
                "coastal-chic",
                "Riviera Linens & Sun-Drenched Neutrals",
                "Breezy open-weave linen shirts, relaxed wide-leg trousers, woven straw totes, and effortless seaside resort tailoring.",
                "https://images.unsplash.com/photo-1509631179647-0177331693ae?w=1400&q=80",
                "#A5C4D4",
                6,
                true
        );
    }

    private void createOrUpdateCollection(String id, String name, String slug, String tagline, String desc, String heroUrl, String accentColor, int order, boolean featured) {
        collectionRepository.findBySlug(slug).ifPresentOrElse(
                col -> {
                    col.setName(name);
                    col.setHeroMediaUrl(heroUrl);
                    col.setTagline(tagline);
                    col.setDescription(desc);
                    col.setAccentColor(accentColor);
                    col.setIsFeatured(featured);
                    col.setIsActive(true);
                    col.setDisplayOrder(order);
                    collectionRepository.save(col);
                },
                () -> {
                    Collection col = Collection.builder()
                            .id(id)
                            .name(name)
                            .slug(slug)
                            .tagline(tagline)
                            .description(desc)
                            .heroMediaUrl(heroUrl)
                            .accentColor(accentColor)
                            .displayOrder(order)
                            .isFeatured(featured)
                            .isActive(true)
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();
                    collectionRepository.save(col);
                }
        );
    }

    private void seedProductsAndVariants() {
        Category menCat = categoryRepository.findBySlug("men").orElse(null);
        Category womenCat = categoryRepository.findBySlug("women").orElse(null);
        Category kidsCat = categoryRepository.findBySlug("kids").orElse(null);

        Map<String, Collection> collectionMap = new HashMap<>();
        collectionRepository.findAll().forEach(c -> collectionMap.put(c.getSlug(), c));

        List<Product> productsToSave = new ArrayList<>();
        List<ProductVariant> variantsToSave = new ArrayList<>();
        List<CollectionProduct> collectionProductsToSave = new ArrayList<>();

        // ==================== MEN'S PRODUCTS (20 Products) ====================
        buildDemoProduct("mp-1", "Structured Wool Double-Breasted Blazer", "structured-wool-double-breasted-blazer",
                "Crafted from premium 120s virgin Italian wool, this double-breasted blazer delivers a commanding shoulder silhouette with natural waist suppression.",
                menCat, "outerwear", "890.00",
                List.of("https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=900&q=80", "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=900&q=80"),
                List.of("Midnight Navy", "Charcoal Melange"), List.of("38R", "40R", "42R", "44R"),
                List.of("old-money", "minimal-luxe"), collectionMap, "Men", "Atelier Savile",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("mp-2", "Pleated Wide-Leg Wool Trousers", "pleated-wide-leg-wool-trousers",
                "Relaxed architectural fit with deep front double pleats, cut in midweight cavalry twill. Drapes cleanly over structured loafers.",
                menCat, "wide-leg-trousers", "380.00",
                List.of("https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=900&q=80", "https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=900&q=80"),
                List.of("Oatmeal Beige", "Deep Espresso", "Ash Charcoal"), List.of("30", "32", "34", "36"),
                List.of("old-money", "minimal-luxe", "quiet-luxury"), collectionMap, "Men", "L’Étoile Studio",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("mp-3", "Handmade Suede Horsebit Loafers", "handmade-suede-horsebit-loafers",
                "Tuscan calf suede with antiqued brass horsebit hardware. Hand-stitched apron with flexible Blake-stitched leather soles.",
                menCat, "shoes", "520.00",
                List.of("https://images.unsplash.com/photo-1614252235316-8c857d38b5f4?w=900&q=80", "https://images.unsplash.com/photo-1533867617858-e7b97e060509?w=900&q=80"),
                List.of("Snuff Suede", "Dark Chocolate Suede"), List.of("40", "41", "42", "43", "44"),
                List.of("old-money", "quiet-luxury"), collectionMap, "Men", "Cobbler & Son",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("mp-4", "Pure Cashmere Rollneck Sweater", "pure-cashmere-rollneck-sweater",
                "Knitted from 4-ply Grade-A Mongolian cashmere with ribbed cuffs and collar. Cloud-like thermal warmth without bulk.",
                menCat, "knitwear", "640.00",
                List.of("https://images.unsplash.com/photo-1617137984095-74e4e5e3613f?w=900&q=80", "https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=900&q=80"),
                List.of("Ivory Cream", "Heather Grey", "Midnight Black"), List.of("S", "M", "L", "XL"),
                List.of("quiet-luxury", "minimal-luxe"), collectionMap, "Men", "Nobilis Knitwear",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("mp-5", "Relaxed Heavyweight Cotton Overshirt", "relaxed-heavyweight-cotton-overshirt",
                "Structured boxy fit tailored from 380gsm garment-dyed Japanese cotton twill with matte horn buttons.",
                menCat, "shirts", "290.00",
                List.of("https://images.unsplash.com/photo-1596755094514-f87e34085b2c?w=900&q=80"),
                List.of("Washed Olive", "Desert Sand"), List.of("S", "M", "L", "XL"),
                List.of("street-couture", "dark-academia"), collectionMap, "Men", "Forme Moderne",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("mp-6", "Slim Tailored Italian Chinos", "slim-tailored-italian-chinos",
                "Clean tapered silhouette tailored from stretch cotton-gabardine with interior curtain waistband.",
                menCat, "close-leg-trousers", "240.00",
                List.of("https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=900&q=80"),
                List.of("Stone Khaki", "Navy Blue"), List.of("30", "32", "34", "36"),
                List.of("old-money", "coastal-chic"), collectionMap, "Men", "Sartoria Napoli",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("mp-7", "Heavyweight Supima Cotton Tee", "heavyweight-supima-cotton-tee",
                "260gsm long-staple American Supima cotton with seamless ribbed collar and relaxed drop-shoulder.",
                menCat, "t-shirts", "110.00",
                List.of("https://images.unsplash.com/photo-1521572267360-ee0c2909d518?w=900&q=80"),
                List.of("Optic White", "Charcoal Slate"), List.of("S", "M", "L", "XL"),
                List.of("minimal-luxe", "street-couture"), collectionMap, "Men", "Atelier Base",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("mp-8", "Architectural Raglan Trench Coat", "architectural-raglan-trench-coat",
                "Water-repellent bonded cotton gabardine with oversized storm flap, horn buckles, and fluid drape.",
                menCat, "outerwear", "1150.00",
                List.of("https://images.unsplash.com/photo-1544441893-675973e31985?w=900&q=80"),
                List.of("Honey Beige", "Black Noir"), List.of("38", "40", "42", "44"),
                List.of("minimal-luxe", "dark-academia"), collectionMap, "Men", "Maison Struct",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("mp-9", "Linen Camp-Collar Resort Shirt", "linen-camp-collar-resort-shirt",
                "Airy French flax linen with relaxed Cuban open collar and mother-of-pearl buttons.",
                menCat, "shirts", "220.00",
                List.of("https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=900&q=80"),
                List.of("Crisp White", "Sky Blue Stripe"), List.of("S", "M", "L", "XL"),
                List.of("coastal-chic"), collectionMap, "Men", "Riviera Atelier",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("mp-10", "Classic Harris Tweed Blazer", "classic-harris-tweed-blazer",
                "Handwoven Scottish virgin wool tweed with authentic elbow patches and herringbone weave.",
                menCat, "outerwear", "780.00",
                List.of("https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=900&q=80"),
                List.of("Moss Green Herringbone", "Tweed Brown"), List.of("38R", "40R", "42R", "44R"),
                List.of("dark-academia", "old-money"), collectionMap, "Men", "Heritage Mills",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("mp-11", "Minimalist Leather Court Sneakers", "minimalist-leather-court-sneakers",
                "Monochromatic low-top court sneakers constructed from full-grain calfskin with Margom rubber cupsole.",
                menCat, "shoes", "340.00",
                List.of("https://images.unsplash.com/photo-1560769629-975ec94e6a86?w=900&q=80"),
                List.of("Chalk White", "All Black"), List.of("40", "41", "42", "43", "44"),
                List.of("minimal-luxe", "street-couture"), collectionMap, "Men", "Studio Minimal",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("mp-12", "Merino Wool Cable-Knit Crewneck", "merino-wool-cable-knit-crewneck",
                "Heavyweight 7-gauge British merino wool featuring intricate cable-stitch motifs and reinforced crewneck.",
                menCat, "knitwear", "420.00",
                List.of("https://images.unsplash.com/photo-1578632767115-351597cf2477?w=900&q=80"),
                List.of("Ecru Cream", "Forest Moss"), List.of("S", "M", "L", "XL"),
                List.of("old-money", "dark-academia"), collectionMap, "Men", "Heritage Mills",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("mp-13", "Technical Ripstop Field Overshirt", "technical-ripstop-field-overshirt",
                "Japanese utilitarian micro-ripstop with dual 3D cargo chest pockets and Fidlock magnetic snap cuffs.",
                menCat, "shirts", "380.00",
                List.of("https://images.unsplash.com/photo-1548883354-7622d03aca27?w=900&q=80"),
                List.of("Stealth Black", "Battleship Grey"), List.of("S", "M", "L", "XL"),
                List.of("street-couture"), collectionMap, "Men", "Techne Corp",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("mp-14", "Relaxed Linen Drawstring Trouser", "relaxed-linen-drawstring-trouser",
                "Airy relaxed cut in 100% natural European flax with elasticized drawstring waistband.",
                menCat, "wide-leg-trousers", "290.00",
                List.of("https://images.unsplash.com/photo-1512436991641-6745cdb1723f?w=900&q=80"),
                List.of("Natural Flax", "Navy Sand"), List.of("30", "32", "34", "36"),
                List.of("coastal-chic", "old-money"), collectionMap, "Men", "Riviera Atelier",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("mp-15", "Brushed Mohair Gradient Cardigan", "brushed-mohair-gradient-cardigan",
                "Fluffy brushed South African mohair blend with horn buttons and dropped shoulders.",
                menCat, "knitwear", "490.00",
                List.of("https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?w=900&q=80"),
                List.of("Smoke Ombre", "Amber Ochre"), List.of("S", "M", "L", "XL"),
                List.of("street-couture", "dark-academia"), collectionMap, "Men", "Maison Struct",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("mp-16", "Classic Pinpoint Oxford Shirt", "classic-pinpoint-oxford-shirt",
                "Crisp 80s two-ply pinpoint cotton with soft unlined button-down collar and mother of pearl buttons.",
                menCat, "shirts", "185.00",
                List.of("https://images.unsplash.com/photo-1598033129183-c4f50c736f10?w=900&q=80"),
                List.of("Sky Blue", "White", "Pink Oxford"), List.of("15", "15.5", "16", "16.5"),
                List.of("old-money", "dark-academia"), collectionMap, "Men", "Atelier Savile",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("mp-17", "Hand-Burnished Calfskin Chelsea Boots", "hand-burnished-calfskin-chelsea-boots",
                "Goodyear-welted equestrian silhouette with hand-patinated Italian box calf and stacked leather heel.",
                menCat, "shoes", "580.00",
                List.of("https://images.unsplash.com/photo-1638247025967-b4e38f787b76?w=900&q=80"),
                List.of("Chestnut Burnish", "Jet Black"), List.of("40", "41", "42", "43", "44"),
                List.of("old-money", "quiet-luxury"), collectionMap, "Men", "Cobbler & Son",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("mp-18", "Raw Selvedge 14oz Denim Jeans", "raw-selvedge-14oz-denim-jeans",
                "Kuroki Mills raw red-line selvedge denim tailored in a clean straight silhouette with chainstitched hems.",
                menCat, "pants", "275.00",
                List.of("https://images.unsplash.com/photo-1542272604-780c96856592?w=900&q=80"),
                List.of("Deep Indigo"), List.of("30", "32", "34", "36"),
                List.of("street-couture"), collectionMap, "Men", "Forme Moderne",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("mp-19", "Silk-Cashmere Fine Knitted Polo", "silk-cashmere-fine-knitted-polo",
                "18-gauge ultrafine silk-cashmere blend with ribbed polo collar and seamless fashioned armholes.",
                menCat, "t-shirts", "390.00",
                List.of("https://images.unsplash.com/photo-1586363104862-3a5e2ab60d99?w=900&q=80"),
                List.of("Dark Taupe", "Pearl White"), List.of("S", "M", "L", "XL"),
                List.of("quiet-luxury", "coastal-chic"), collectionMap, "Men", "Nobilis Knitwear",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("mp-20", "Belted Double-Face Wool Overcoat", "belted-double-face-wool-overcoat",
                "Unlined double-face virgin wool with wraparound self-tie belt, raglan sleeves, and deep welt pockets.",
                menCat, "outerwear", "1380.00",
                List.of("https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=900&q=80"),
                List.of("Camel Vicuña", "Charcoal Slate"), List.of("38", "40", "42", "44"),
                List.of("quiet-luxury", "minimal-luxe"), collectionMap, "Men", "Nobilis Luxe",
                productsToSave, variantsToSave, collectionProductsToSave);


        // ==================== WOMEN'S PRODUCTS (20 Products) ====================
        buildDemoProduct("wp-1", "Bias-Cut Mulberry Silk Slip Dress", "bias-cut-mulberry-silk-slip-dress",
                "100% 22-momme Mulberry silk cut on the bias for fluid drape. Delicate adjustable straps and softly sculpted neckline.",
                womenCat, "dresses", "620.00",
                List.of("https://images.unsplash.com/photo-1595777457583-95e059d581b8?w=900&q=80", "https://images.unsplash.com/photo-1539109136881-3be0616acf4b?w=900&q=80"),
                List.of("Champagne Gold", "Obsidian Black", "Emerald Olive"), List.of("XS", "S", "M", "L"),
                List.of("minimal-luxe", "quiet-luxury"), collectionMap, "Women", "Maison Soie",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("wp-2", "Sculptural Hourglass Wool Blazer", "sculptural-hourglass-wool-blazer",
                "Architectural cinched waist silhouette with padded shoulders and peaked lapels in compact Italian wool crepe.",
                womenCat, "blazers", "950.00",
                List.of("https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=900&q=80", "https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=900&q=80"),
                List.of("Bone Ivory", "Tuxedo Black"), List.of("34", "36", "38", "40"),
                List.of("minimal-luxe", "old-money"), collectionMap, "Women", "Studio Sculpt",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("wp-3", "High-Rise Fluid Wide-Leg Trousers", "high-rise-fluid-wide-leg-trousers",
                "Floor-sweeping wide-leg silhouette cut in weighty triacetate-crepe with invisible front closure and clean pressed creases.",
                womenCat, "wide-leg-trousers", "410.00",
                List.of("https://images.unsplash.com/photo-1509631179647-0177331693ae?w=900&q=80", "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?w=900&q=80"),
                List.of("Alabaster Cream", "Dark Mocha", "Slate Grey"), List.of("34", "36", "38", "40", "42"),
                List.of("minimal-luxe", "coastal-chic"), collectionMap, "Women", "L’Étoile Studio",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("wp-4", "Pointed-Toe Italian Leather Slingbacks", "pointed-toe-italian-leather-slingbacks",
                "Sculpted 55mm kitten heel with tapered chisel toe and polished gold-tone buckle detailing in supple nappa leather.",
                womenCat, "shoes", "490.00",
                List.of("https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=900&q=80", "https://images.unsplash.com/photo-1535043934128-cf0b28d52f95?w=900&q=80"),
                List.of("Black Nappa", "Burgundy Cherry", "Cream Beige"), List.of("36", "37", "38", "39", "40"),
                List.of("old-money", "minimal-luxe"), collectionMap, "Women", "Calzature Milano",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("wp-5", "Draped Asymmetrical Silk Blouse", "draped-asymmetrical-silk-blouse",
                "Fluid silk georgette blouse with cascading cowl neckline and elongated French cuffs.",
                womenCat, "tops", "370.00",
                List.of("https://images.unsplash.com/photo-1581044777550-4cfa60707c03?w=900&q=80"),
                List.of("Ivory Silk", "Terracotta Rose"), List.of("XS", "S", "M", "L"),
                List.of("minimal-luxe", "quiet-luxury"), collectionMap, "Women", "Maison Soie",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("wp-6", "Tailored Double-Faced Cashmere Coat", "tailored-double-faced-cashmere-coat",
                "Hand-finished double-faced cashmere with tie belt, notch collar, and patch pockets.",
                womenCat, "blazers", "1680.00",
                List.of("https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=900&q=80"),
                List.of("Camel Vicuña", "Charcoal Melange"), List.of("34", "36", "38", "40"),
                List.of("quiet-luxury", "old-money"), collectionMap, "Women", "Nobilis Luxe",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("wp-7", "Pleated A-Line Wool Crepe Skirt", "pleated-a-line-wool-crepe-skirt",
                "Crisply knife-pleated midi skirt in virgin wool crepe with concealed side zip.",
                womenCat, "skirts", "340.00",
                List.of("https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=900&q=80"),
                List.of("Oxford Navy", "Chocolate Brown"), List.of("34", "36", "38", "40"),
                List.of("dark-academia", "old-money"), collectionMap, "Women", "Heritage Mills",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("wp-8", "Structured Leather Mini Vanity Bag", "structured-leather-mini-vanity-bag",
                "Box calfskin structured handbag with gold-plated padlock charm and detachable leather crossbody strap.",
                womenCat, "bags", "680.00",
                List.of("https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=900&q=80"),
                List.of("Black Box Leather", "Cognac Saddle"), List.of("One Size"),
                List.of("quiet-luxury", "minimal-luxe"), collectionMap, "Women", "Cuir Atelier",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("wp-9", "Resort Linen Maxi Shirtdress", "resort-linen-maxi-shirtdress",
                "Floor-length European washed linen dress with mother-of-pearl buttons and relaxed sash belt.",
                womenCat, "dresses", "460.00",
                List.of("https://images.unsplash.com/photo-1515372039744-b8f02a3ae446?w=900&q=80"),
                List.of("Sand Beige", "Aegean Blue"), List.of("XS", "S", "M", "L"),
                List.of("coastal-chic"), collectionMap, "Women", "Riviera Atelier",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("wp-10", "Fine Ribbed Cashmere Knit Polo", "fine-ribbed-cashmere-knit-polo",
                "Featherweight 100% cashmere knit polo with open Johnny collar and ribbed hem.",
                womenCat, "tops", "390.00",
                List.of("https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=900&q=80"),
                List.of("Oatmeal", "Cloud White"), List.of("XS", "S", "M", "L"),
                List.of("quiet-luxury", "coastal-chic"), collectionMap, "Women", "Nobilis Knitwear",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("wp-11", "Sculpted Ribbed Knit Maxi Dress", "sculpted-ribbed-knit-maxi-dress",
                "Body-contouring compact ribbed knit with mock neck and elegant side slit for fluid movement.",
                womenCat, "dresses", "560.00",
                List.of("https://images.unsplash.com/photo-1572804013309-59a88b7e92f1?w=900&q=80"),
                List.of("Anthracite Black", "Warm Taupe"), List.of("XS", "S", "M", "L"),
                List.of("minimal-luxe", "quiet-luxury"), collectionMap, "Women", "Studio Sculpt",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("wp-12", "Oversized Tailored Linen Blazer", "oversized-tailored-linen-blazer",
                "Relaxed boyfriend fit blazer in breathable pure linen with padded dropped shoulders.",
                womenCat, "blazers", "690.00",
                List.of("https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=900&q=80"),
                List.of("Chalk Cream", "Natural Khaki"), List.of("34", "36", "38", "40"),
                List.of("coastal-chic", "old-money"), collectionMap, "Women", "Riviera Atelier",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("wp-13", "High-Waisted Silk Crepe Palazzo Pants", "high-waisted-silk-crepe-palazzo-pants",
                "Voluminous high-rise trousers cut from heavy silk crepe with concealed side zip.",
                womenCat, "wide-leg-trousers", "480.00",
                List.of("https://images.unsplash.com/photo-1509631179647-0177331693ae?w=900&q=80"),
                List.of("Pearl Ivory", "Onyx Black"), List.of("34", "36", "38", "40"),
                List.of("minimal-luxe", "quiet-luxury"), collectionMap, "Women", "L’Étoile Studio",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("wp-14", "Square-Toe Strappy Nappa Sandals", "square-toe-strappy-nappa-sandals",
                "Delicate tubular leather strappy sandals with 60mm architectural heel and cushioned footbed.",
                womenCat, "shoes", "430.00",
                List.of("https://images.unsplash.com/photo-1562273138-f46be4ebdf33?w=900&q=80"),
                List.of("Butter Yellow", "Espresso Leather"), List.of("36", "37", "38", "39", "40"),
                List.of("coastal-chic", "minimal-luxe"), collectionMap, "Women", "Calzature Milano",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("wp-15", "Crisp Cotton Poplin Oversized Shirt", "crisp-cotton-poplin-oversized-shirt",
                "Tailored from high-density Egyptian cotton poplin with exaggerated cuffs and curved hem.",
                womenCat, "tops", "295.00",
                List.of("https://images.unsplash.com/photo-1608256246200-53e635b5b65f?w=900&q=80"),
                List.of("Optic White", "French Blue Stripe"), List.of("XS", "S", "M", "L"),
                List.of("old-money", "minimal-luxe"), collectionMap, "Women", "Atelier Base",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("wp-16", "Heavyweight Wool Houndstooth Coat", "heavyweight-wool-houndstooth-coat",
                "Heritage tailored knee-length coat featuring subtle micro-houndstooth weave and horn buttons.",
                womenCat, "blazers", "1250.00",
                List.of("https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=900&q=80"),
                List.of("Black/Ecru Houndstooth"), List.of("34", "36", "38", "40"),
                List.of("dark-academia", "old-money"), collectionMap, "Women", "Heritage Mills",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("wp-17", "Bias-Cut Silk Satin Column Skirt", "bias-cut-silk-satin-column-skirt",
                "Luminous heavyweight silk satin maxi skirt designed with clean elastic waist and fluid movement.",
                womenCat, "skirts", "360.00",
                List.of("https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=900&q=80"),
                List.of("Champagne", "Obsidian"), List.of("34", "36", "38", "40"),
                List.of("minimal-luxe", "quiet-luxury"), collectionMap, "Women", "Maison Soie",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("wp-18", "Artisan Woven Calfskin Shoulder Bag", "artisan-woven-calfskin-shoulder-bag",
                "Intricately hand-woven nappa leather shoulder bag with magnetic flap closure and gold-tone chain strap.",
                womenCat, "bags", "890.00",
                List.of("https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=900&q=80"),
                List.of("Caramel Tan", "Cream White"), List.of("One Size"),
                List.of("quiet-luxury", "coastal-chic"), collectionMap, "Women", "Cuir Atelier",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("wp-19", "Seamless Cashmere Turtleneck Sweater", "seamless-cashmere-turtleneck-sweater",
                "Ultra-soft 3D knit seamless pure cashmere turtleneck with fitted wrists and clean lines.",
                womenCat, "tops", "520.00",
                List.of("https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=900&q=80"),
                List.of("Dove Grey", "Midnight Black"), List.of("XS", "S", "M", "L"),
                List.of("quiet-luxury", "minimal-luxe"), collectionMap, "Women", "Nobilis Knitwear",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("wp-20", "Equestrian Leather Knee-High Boots", "equestrian-leather-knee-high-boots",
                "Supple polished calfskin riding boots with pull-tabs, sculpted ankle fit, and durable leather soles.",
                womenCat, "shoes", "790.00",
                List.of("https://images.unsplash.com/photo-1543163521-1bf539c55dd2?w=900&q=80"),
                List.of("Cognac Brown", "Jet Black"), List.of("36", "37", "38", "39", "40"),
                List.of("old-money", "dark-academia"), collectionMap, "Women", "Cobbler & Son",
                productsToSave, variantsToSave, collectionProductsToSave);


        // ==================== KIDS' PRODUCTS (20 Products) ====================
        buildDemoProduct("kp-1", "Mini Organic Cotton Knit Cardigan", "mini-organic-cotton-knit-cardigan",
                "Chunky knit cardigan made with GOTS-certified organic cotton and natural wood buttons.",
                kidsCat, "knitwear", "140.00",
                List.of("https://images.unsplash.com/photo-1514090458221-65bb69cf63e6?w=900&q=80"),
                List.of("Oatmeal Heather", "Sage Green"), List.of("2Y", "4Y", "6Y", "8Y"),
                List.of("quiet-luxury", "coastal-chic"), collectionMap, "Kids", "Petit Atelier",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("kp-2", "Junior Tailored Linen Blend Set", "junior-tailored-linen-blend-set",
                "Matching collar shirt and pleated shorts in breezy linen-cotton blend for celebrations.",
                kidsCat, "sets", "195.00",
                List.of("https://images.unsplash.com/photo-1503919545889-aef636e10ad4?w=900&q=80"),
                List.of("Warm Sand", "Powder Blue"), List.of("3Y", "5Y", "7Y", "9Y"),
                List.of("coastal-chic", "old-money"), collectionMap, "Kids", "Petit Savile",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("kp-3", "Mini Trench Coat in Khaki Gabardine", "mini-trench-coat-in-khaki-gabardine",
                "Scaled-down heritage trench coat in water-resistant twill with checked cotton lining.",
                kidsCat, "outerwear", "260.00",
                List.of("https://images.unsplash.com/photo-1519238263530-99bdd11df2ea?w=900&q=80"),
                List.of("Classic Khaki", "Navy"), List.of("4Y", "6Y", "8Y", "10Y"),
                List.of("old-money", "minimal-luxe"), collectionMap, "Kids", "Petit Atelier",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("kp-4", "Organic Waffle-Knit Sweatshirt & Joggers", "organic-waffle-knit-sweatshirt-joggers",
                "Super-soft thermal waffle set with elasticized drawstring waist and reinforced knee patches.",
                kidsCat, "sets", "120.00",
                List.of("https://images.unsplash.com/photo-1543854589-ab99447475f4?w=900&q=80"),
                List.of("Charcoal", "Dusty Rose"), List.of("2Y", "4Y", "6Y", "8Y"),
                List.of("minimal-luxe"), collectionMap, "Kids", "Petit Base",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("kp-5", "Classic Leather Junior Chelsea Boots", "classic-leather-junior-chelsea-boots",
                "Supple leather boots with flexible elastic side gussets and non-slip rubber soles.",
                kidsCat, "shoes", "175.00",
                List.of("https://images.unsplash.com/photo-1514989940723-e8e51635b782?w=900&q=80"),
                List.of("Tan Leather", "Black Leather"), List.of("28", "30", "32", "34"),
                List.of("dark-academia", "old-money"), collectionMap, "Kids", "Cobbler Junior",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("kp-6", "Breton Stripe Cotton Tee", "breton-stripe-cotton-tee",
                "Pure combed cotton sailor stripe long-sleeve tee with shoulder snap buttons for easy dressing.",
                kidsCat, "tops", "65.00",
                List.of("https://images.unsplash.com/photo-1471286174890-9c112ffca56a?w=900&q=80"),
                List.of("Navy/White Stripe", "Red/White Stripe"), List.of("2Y", "4Y", "6Y", "8Y"),
                List.of("coastal-chic"), collectionMap, "Kids", "Petit Atelier",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("kp-7", "Mini Corduroy Pleated Trousers", "mini-corduroy-pleated-trousers",
                "Soft fine-wale corduroy trousers with adjustable inner elastic waistband and front pleats.",
                kidsCat, "pants", "110.00",
                List.of("https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?w=900&q=80"),
                List.of("Chestnut Brown", "Forest Green"), List.of("3Y", "5Y", "7Y", "9Y"),
                List.of("dark-academia"), collectionMap, "Kids", "Petit Savile",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("kp-8", "Soft Cashmere Blend Beanie & Scarf Set", "soft-cashmere-blend-beanie-scarf-set",
                "Ultra-gentle ribbed cashmere and merino wool winter accessory set for toddlers and kids.",
                kidsCat, "knitwear", "95.00",
                List.of("https://images.unsplash.com/photo-1514090458221-65bb69cf63e6?w=900&q=80"),
                List.of("Cream Ivory", "Charcoal Grey"), List.of("One Size"),
                List.of("quiet-luxury"), collectionMap, "Kids", "Petit Atelier",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("kp-9", "Organic Cotton Breton Striped Sailor Romper", "organic-cotton-breton-striped-sailor-romper",
                "Breezy short-sleeve romper with nautical blue stripes and nickel-free snap fastenings.",
                kidsCat, "sets", "85.00",
                List.of("https://images.unsplash.com/photo-1522771930-78848d9293e8?w=900&q=80"),
                List.of("Nautical Navy", "Sky Blue"), List.of("6M", "12M", "18M", "24M"),
                List.of("coastal-chic"), collectionMap, "Kids", "Petit Atelier",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("kp-10", "Toddler Quilted Lightweight Jacket", "toddler-quilted-lightweight-jacket",
                "Diamond-quilted jacket with corduroy collar trim, patch pockets, and snap button closure.",
                kidsCat, "outerwear", "175.00",
                List.of("https://images.unsplash.com/photo-1622290291468-a28f7a7dc6a8?w=900&q=80"),
                List.of("Olive Green", "Deep Navy"), List.of("2Y", "4Y", "6Y", "8Y"),
                List.of("old-money", "minimal-luxe"), collectionMap, "Kids", "Petit Savile",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("kp-11", "Chunky Merino Wool Cable Knit Sweater", "chunky-merino-wool-cable-knit-sweater",
                "Heritage Aran cable knit sweater spun from soft non-itch merino wool for colder days.",
                kidsCat, "knitwear", "160.00",
                List.of("https://images.unsplash.com/photo-1514090458221-65bb69cf63e6?w=900&q=80"),
                List.of("Ecru Cream", "Camel Tan"), List.of("3Y", "5Y", "7Y", "9Y"),
                List.of("dark-academia", "old-money"), collectionMap, "Kids", "Petit Atelier",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("kp-12", "Kids Classic Suede Penny Loafers", "kids-classic-suede-penny-loafers",
                "Handcrafted soft suede slip-on penny loafers with flexible natural crepe rubber soles.",
                kidsCat, "shoes", "145.00",
                List.of("https://images.unsplash.com/photo-1514989940723-e8e51635b782?w=900&q=80"),
                List.of("Snuff Brown", "Navy Blue"), List.of("28", "30", "32", "34"),
                List.of("old-money", "quiet-luxury"), collectionMap, "Kids", "Cobbler Junior",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("kp-13", "Linen Button-Front Shirt & Shorts Set", "linen-button-front-shirt-shorts-set",
                "Two-piece vacation set crafted from washed linen with elastic waist shorts.",
                kidsCat, "sets", "135.00",
                List.of("https://images.unsplash.com/photo-1503919545889-aef636e10ad4?w=900&q=80"),
                List.of("Chalk White", "Sage Stripe"), List.of("2Y", "4Y", "6Y", "8Y"),
                List.of("coastal-chic"), collectionMap, "Kids", "Petit Atelier",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("kp-14", "Vintage Corduroy Dungaree Overalls", "vintage-corduroy-dungaree-overalls",
                "Durable fine-rib corduroy overalls with metal clasps and deep front pouch pockets.",
                kidsCat, "pants", "125.00",
                List.of("https://images.unsplash.com/photo-1508214751196-bcfd4ca60f91?w=900&q=80"),
                List.of("Ochre Mustard", "Navy"), List.of("18M", "2Y", "3Y", "4Y"),
                List.of("dark-academia"), collectionMap, "Kids", "Petit Base",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("kp-15", "Junior Waterproof Hooded Rain Mac", "junior-waterproof-hooded-rain-mac",
                "Matte water-resistant polyurethane jacket with soft striped cotton lining and storm hood.",
                kidsCat, "outerwear", "150.00",
                List.of("https://images.unsplash.com/photo-1519238263530-99bdd11df2ea?w=900&q=80"),
                List.of("Yellow Ochre", "Midnight Navy"), List.of("3Y", "5Y", "7Y", "9Y"),
                List.of("minimal-luxe"), collectionMap, "Kids", "Petit Atelier",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("kp-16", "Peter Pan Collar Embroidered Blouse", "peter-pan-collar-embroidered-blouse",
                "Organic cotton poplin blouse featuring scalloped Peter Pan collar and delicate floral stitching.",
                kidsCat, "tops", "88.00",
                List.of("https://images.unsplash.com/photo-1471286174890-9c112ffca56a?w=900&q=80"),
                List.of("Ivory White", "Pale Blush"), List.of("2Y", "4Y", "6Y", "8Y"),
                List.of("old-money"), collectionMap, "Kids", "Petit Savile",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("kp-17", "Kids Minimalist Leather Low-Top Sneakers", "kids-minimalist-leather-low-top-sneakers",
                "Premium leather low-top sneakers with double Velcro straps for effortless on-and-off.",
                kidsCat, "shoes", "115.00",
                List.of("https://images.unsplash.com/photo-1514989940723-e8e51635b782?w=900&q=80"),
                List.of("All White", "White/Gum"), List.of("26", "28", "30", "32"),
                List.of("minimal-luxe", "street-couture"), collectionMap, "Kids", "Petit Base",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("kp-18", "Pleated Tartan Plaid Skirt Set", "pleated-tartan-plaid-skirt-set",
                "Traditional Scottish wool blend pleated skirt with matching elastic headband.",
                kidsCat, "pants", "110.00",
                List.of("https://images.unsplash.com/photo-1543854589-ab99447475f4?w=900&q=80"),
                List.of("Royal Stewart Plaid", "Black Watch"), List.of("3Y", "5Y", "7Y", "9Y"),
                List.of("dark-academia"), collectionMap, "Kids", "Petit Savile",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("kp-19", "Fine Gauge Cashmere Crewneck for Kids", "fine-gauge-cashmere-crewneck-for-kids",
                "Cloud-soft 100% cashmere knit pullover designed for delicate skin with rolled hem accents.",
                kidsCat, "knitwear", "180.00",
                List.of("https://images.unsplash.com/photo-1514090458221-65bb69cf63e6?w=900&q=80"),
                List.of("Oatmeal", "Soft Grey"), List.of("2Y", "4Y", "6Y", "8Y"),
                List.of("quiet-luxury"), collectionMap, "Kids", "Petit Atelier",
                productsToSave, variantsToSave, collectionProductsToSave);

        buildDemoProduct("kp-20", "Sun Embroidered Straw Hat & Playsuit Set", "sun-embroidered-straw-hat-playsuit-set",
                "Breathable woven cotton playsuit paired with a matching ribbon-trimmed straw sun hat.",
                kidsCat, "sets", "125.00",
                List.of("https://images.unsplash.com/photo-1522771930-78848d9293e8?w=900&q=80"),
                List.of("Natural Sand", "Terracotta"), List.of("12M", "18M", "2Y", "3Y"),
                List.of("coastal-chic"), collectionMap, "Kids", "Petit Atelier",
                productsToSave, variantsToSave, collectionProductsToSave);

        if (!productsToSave.isEmpty()) {
            log.info("Batch saving {} new products with cascaded variants and collections...", productsToSave.size());
            productRepository.saveAll(productsToSave);
        }

        log.info("Finished seeding demo products! Total products in DB: {}", productRepository.count());
    }

    private void buildDemoProduct(
            String id,
            String name,
            String slug,
            String description,
            Category dynamicCategory,
            String itemTypeSlug,
            String priceStr,
            List<String> mediaUrls,
            List<String> colors,
            List<String> sizes,
            List<String> collectionSlugs,
            Map<String, Collection> collectionMap,
            String gender,
            String brandName,
            List<Product> productsToSave,
            List<ProductVariant> variantsToSave,
            List<CollectionProduct> collectionProductsToSave) {

        if (productRepository.findBySlug(slug).isPresent()) {
            return;
        }

        ItemType itemType = null;
        if (dynamicCategory != null) {
            itemType = itemTypeRepository.findByCategoryIdAndSlug(dynamicCategory.getId(), itemTypeSlug).orElse(null);
        }

        BigDecimal price = new BigDecimal(priceStr);
        String primaryMedia = mediaUrls.isEmpty() ? null : mediaUrls.get(0);
        String mediaIds = String.join(",", mediaUrls);
        Instant now = Instant.now();

        Product product = Product.builder()
                .id(id)
                .brandId("brand_" + brandName.toLowerCase().replaceAll("[^a-z0-9]", "_"))
                .name(name)
                .slug(slug)
                .summary(description.length() > 120 ? description.substring(0, 117) + "..." : description)
                .description(description)
                .category(dynamicCategory != null ? dynamicCategory.getSlug() : "men")
                .subcategory(itemTypeSlug)
                .productType(itemTypeSlug)
                .status("ACTIVE")
                .price(price)
                .currency("USD")
                .primaryMediaId(primaryMedia)
                .mediaIds(mediaIds)
                .galleryMediaIds(mediaIds)
                .gender(gender)
                .color(colors.isEmpty() ? "Standard" : colors.get(0))
                .size(sizes.isEmpty() ? "Standard" : sizes.get(0))
                .material("Premium Craft")
                .dynamicCategory(dynamicCategory)
                .dynamicItemType(itemType)
                .variants(new ArrayList<>())
                .collectionProducts(new ArrayList<>())
                .createdAt(now)
                .updatedAt(now)
                .build();

        // Variants
        int variantIndex = 1;
        for (String color : colors) {
            for (String size : sizes) {
                String sku = (slug.toUpperCase().replaceAll("[^A-Z0-9]", "-") + "-" + color.substring(0, Math.min(3, color.length())).toUpperCase() + "-" + size).replace("--", "-");
                ProductVariant variant = ProductVariant.builder()
                        .product(product)
                        .sku(sku + "-" + variantIndex++)
                        .colorName(color)
                        .colorHex("#222222")
                        .size(size)
                        .priceOverride(price)
                        .stockQuantity(50)
                        .mediaIds(primaryMedia)
                        .isActive(true)
                        .createdAt(now)
                        .updatedAt(now)
                        .build();
                product.getVariants().add(variant);
            }
        }

        // Collection Associations
        for (int i = 0; i < collectionSlugs.size(); i++) {
            String colSlug = collectionSlugs.get(i);
            Collection col = collectionMap.get(colSlug);
            if (col != null) {
                CollectionProduct.CollectionProductId cpId = new CollectionProduct.CollectionProductId(col.getId(), product.getId());
                CollectionProduct cp = CollectionProduct.builder()
                        .id(cpId)
                        .collection(col)
                        .product(product)
                        .displayOrder(i + 1)
                        .curatorNote("Editorial Pick for " + col.getName())
                        .build();
                product.getCollectionProducts().add(cp);
            }
        }

        productsToSave.add(product);
    }
}
