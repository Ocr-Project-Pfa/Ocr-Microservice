package isme.pfaextract.utils;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

public class MoroccanDataConstants {
    public static final Set<String> CITIES = new HashSet<>(Arrays.asList(
            // Major cities
            "AGADIR", "CASABLANCA", "FES", "MARRAKECH", "MEKNES", "OUJDA",
            "RABAT", "SALE", "TANGER", "TETOUAN", "OUARZAZATE", "NADOR",
            "KENITRA", "EL JADIDA", "SAFI", "MOHAMMEDIA", "ESSAOUIRA",
            // Secondary cities
            "TAZA", "ASSILAH", "AL HOCEIMA", "LARACHE", "TIZNIT",
            "BERKANE", "TAOURIRT", "GUERCIF", "FIGUIG", "OUED ZEM",
            "YOUSSOUFIA", "KHOURIBGA", "BENI MELLAL", "ERRACHIDIA",
            // Additional cities
            "CHEFCHAOUEN", "IFRANE", "AZROU", "MIDELT", "TINGHIR",
            "ZAGORA", "TAROUDANT", "TATA", "DAKHLA", "LAAYOUNE",
            "SETTAT", "BERRECHID", "TEMARA", "KHEMISSET", "SKHIRAT"
    ));

    public static final Set<String> COMMON_FIRST_NAMES = new HashSet<>(Arrays.asList(
            // Male names
            "MOHAMMED", "AHMED", "YOUSSEF", "HASSAN", "HAMZA",
            "AMINE", "KARIM", "OMAR", "SAID", "MOUHCINE",
            "RACHID", "KHALID", "MEHDI", "MUSTAPHA", "ABDELLAH",
            "NOUREDDINE", "JAMAL", "BRAHIM", "DRISS", "FOUAD",
            // Female names
            "ZAINEB", "FATIMA", "AICHA", "MERYEM", "KHADIJA",
            "SARA", "HANANE", "SANAA", "NAIMA", "SAMIRA",
            "LAILA", "NADIA", "AMINA", "HAFIDA", "MALIKA",
            "HOUDA", "KARIMA", "SAIDA", "NAJAT", "RACHIDA"
    ));

    public static final Set<String> COMMON_LAST_NAMES = new HashSet<>(Arrays.asList(
            // Traditional family names
            "ALAMI", "IDRISSI", "BENJELLOUN", "TAZI", "FASSI",
            "BERRADA", "CHRAIBI", "BENNANI", "TAHIRI", "ZIANI",
            "EMSAMANI", "ALAOUI", "FILALI", "SEBTI", "LAMRANI",
            // Common surnames
            "EL AMRANI", "BENNIS", "LAHLOU", "BELKADI", "CHERKAOUI",
            "MANSOURI", "BOUAZZAOUI", "HASSANI", "KARIMI", "SAIDI",
            "BENHADDOU", "OUAZZANI", "SQALLI", "BENNASSER", "LAZRAK",
            // Regional surnames
            "RIFFI", "SOUSSI", "SAHLI", "JABRI", "TADLAOUI",
            "GHAZOUANI", "RGUIBI", "SAHRAOUI", "CHAMALI", "DOUKKALI"
    ));

    public static final Set<String> ID_CARD_KEYWORDS = new HashSet<>(Arrays.asList(
            // French keywords
            "ROYAUME DU MAROC", "CARTE NATIONALE", "D'IDENTITE",
            "NE LE", "A", "VALABLE JUSQU'AU", "SEXE",
            // Arabic keywords
            "المملكة المغربية", "البطاقة الوطنية", "للتعريف",
            "الاسم", "تاريخ الازدياد", "محل الازدياد", "رقم"
    ));

    public static final Set<String> DATE_MARKERS = new HashSet<>(Arrays.asList(
            "NE LE", "VALABLE JUSQU'AU", "تاريخ الازدياد",
            "صالحة إلى غاية", "DATE DE NAISSANCE", "DATE D'EXPIRATION"
    ));
}