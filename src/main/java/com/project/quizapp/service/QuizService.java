package com.project.quizapp.service;

import com.project.quizapp.dto.QuestionDTO;
import com.project.quizapp.dto.QuestionReviewDTO;
import com.project.quizapp.dto.QuizRequest;
import com.project.quizapp.dto.QuizResultResponse;
import com.project.quizapp.model.Question;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 20;

    private final List<Question> questionBank = new ArrayList<>();
    private final Map<Long, Question> questionById = new HashMap<>();

    public QuizService() {
        initQuestionBank();
        questionBank.forEach(question -> questionById.put(question.getId(), question));
    }

    private void initQuestionBank() {
        // ==========================================
        // 1. หมวดหมู่ ANIME & GAMING
        // ==========================================
        questionBank.add(new Question(101L, "ANIME_GAMING", "EASY",
                "ในเรื่อง Pokémon โปเกมอนคู่หูตัวแรกของ 'ซาโตชิ' คือตัวใด?",
                List.of("คาเม็กซ์", "พิคาชู", "ลิซาร์ดอน", "ฟุชิกิดาเนะ"), 1,
                "พิคาชูเป็นโปเกมอนคู่หูที่ซาโตชิได้รับจาก ดร.ออคิดส์ในอนิเมะ Pokémon"));

        questionBank.add(new Question(102L, "ANIME_GAMING", "EASY",
                "ในเกม Minecraft แร่ชนิดใดที่นำมาอัปเกรดอุปกรณ์เพชรได้?",
                List.of("Netherite", "Emerald", "Redstone", "Lapis Lazuli"), 0,
                "Netherite ใช้ผสมกับอุปกรณ์เพชรเพื่ออัปเกรดเป็นอุปกรณ์ Netherite"));

        questionBank.add(new Question(103L, "ANIME_GAMING", "EASY",
                "ในดาบพิฆาตอสูร (Demon Slayer) ดาบของทันจิโร่เปลี่ยนเป็นสีอะไรเมื่อถือครองครั้งแรก?",
                List.of("สีแดง", "สีน้ำเงิน", "สีดำ", "สีเหลือง"), 2,
                "ดาบนิชิรินของทันจิโร่เปลี่ยนเป็นสีดำ ซึ่งเป็นสีที่พบได้ไม่บ่อย"));

        questionBank.add(new Question(104L, "ANIME_GAMING", "EASY",
                "ในเกม Genshin Impact เมือง Mondstadt มีเทพแห่งธาตุใดปกครอง?",
                List.of("ธาตุหิน (Geo)", "ธาตุลม (Anemo)", "ธาตุไฟฟ้า (Electro)", "ธาตุไม้ (Dendro)"), 1,
                "Mondstadt เป็นเมืองแห่งอิสรภาพที่อยู่ภายใต้เทพแห่งลม Barbatos (Venti)"));

        questionBank.add(new Question(105L, "ANIME_GAMING", "MEDIUM",
                "ในเรื่อง One Piece ผลปีศาจของลูฟี่ที่มีชื่อจริงว่า Hito Hito no Mi, Model: Nika เดิมเรียกว่าอะไร?",
                List.of("Mera Mera no Mi", "Gomu Gomu no Mi", "Ope Ope no Mi", "Gura Gura no Mi"), 1,
                "ชื่อที่ใช้กันมายาวนานของผลปีศาจของลูฟี่คือ Gomu Gomu no Mi ก่อนการเปิดเผยชื่อจริง"));

        questionBank.add(new Question(106L, "ANIME_GAMING", "MEDIUM",
                "ในเกม Elden Ring บอสที่มีฉายาว่า 'Blade of Miquella' คือใคร?",
                List.of("Ranni", "Radahn", "Malenia", "Morgott"), 2,
                "Malenia มีฉายาว่า Blade of Miquella และเป็นหนึ่งในบอสที่มีชื่อเสียงเรื่องความยาก"));

        questionBank.add(new Question(107L, "ANIME_GAMING", "MEDIUM",
                "ใน Attack on Titan อุปกรณ์เคลื่อนย้ายสามมิติใช้ก๊าซแรงดันสูงที่เกี่ยวข้องกับทรัพยากรใด?",
                List.of("Iceburst Stone", "มีเทนบริสุทธิ์", "ไอน้ำ", "ไฮโดรเจน"), 0,
                "ระบบขับเคลื่อนของอุปกรณ์เคลื่อนย้ายสามมิติใช้ก๊าซจาก Iceburst Stone เป็นองค์ประกอบสำคัญของโลกเรื่อง"));

        questionBank.add(new Question(108L, "ANIME_GAMING", "MEDIUM",
                "ในเกม Valorant Agent สาย Controller จากประเทศกานาคือใคร?",
                List.of("Omen", "Brimstone", "Astra", "Viper"), 2,
                "Astra เป็น Agent ชาวกานาที่ใช้พลังแห่งดวงดาวในการควบคุมพื้นที่"));

        questionBank.add(new Question(109L, "ANIME_GAMING", "HARD",
                "ดาบเหล็กยักษ์ประจำตัวของ Guts จากเรื่อง Berserk มีชื่อว่าอะไร?",
                List.of("Dragonslayer", "Excalibur", "Elucidator", "Zangetsu"), 0,
                "Dragonslayer คือดาบขนาดมหึมาที่เป็นอาวุธคู่กายของ Guts"));

        questionBank.add(new Question(110L, "ANIME_GAMING", "HARD",
                "ในเกม Dark Souls ภาคแรก เมืองหลวงของเทพเจ้าคือสถานที่ใด?",
                List.of("Yharnam", "Anor Londo", "Drangleic", "Lothric"), 1,
                "Anor Londo คือเมืองแห่งเทพเจ้าที่เป็นพื้นที่สำคัญของ Dark Souls"));

        questionBank.add(new Question(111L, "ANIME_GAMING", "HARD",
                "ใน Fate/stay night วิญญาณวีรชน Archer (เอมิยะ) มี Noble Phantasm หลักคืออะไร?",
                List.of("Gate of Babylon", "Unlimited Blade Works", "Excalibur Morgan", "Enuma Elish"), 1,
                "Unlimited Blade Works คือ Reality Marble ที่จำลองโลกภายในซึ่งเต็มไปด้วยอาวุธ"));

        questionBank.add(new Question(112L, "ANIME_GAMING", "HARD",
                "ใน Final Fantasy VII ดาบ Buster Sword เดิมเป็นของใครก่อนส่งต่อมายัง Zack และ Cloud?",
                List.of("Sephiroth", "Genesis Rhapsodos", "Angeal Hewley", "Professor Hojo"), 2,
                "Angeal Hewley เป็นผู้ครอบครอง Buster Sword ก่อนที่ดาบจะส่งต่อไปยัง Zack และต่อมายัง Cloud"));

        // ==========================================
        // 2. GENERAL KNOWLEDGE
        // ==========================================
        questionBank.add(new Question(201L, "GENERAL", "EASY",
                "ประเทศใดมีขนาดพื้นที่ทางภูมิศาสตร์ใหญ่ที่สุดในโลก?",
                List.of("แคนาดา", "จีน", "รัสเซีย", "สหรัฐอเมริกา"), 2,
                "รัสเซียมีพื้นที่รวมประมาณ 17.1 ล้านตารางกิโลเมตรและเป็นประเทศที่ใหญ่ที่สุดตามพื้นที่"));

        questionBank.add(new Question(202L, "GENERAL", "EASY",
                "ธาตุเคมีที่มีสัญลักษณ์ว่า 'Au' คือธาตุใด?",
                List.of("เงิน (Silver)", "ทองคำ (Gold)", "ทองแดง (Copper)", "อลูมิเนียม (Aluminum)"), 1,
                "Au มาจากภาษาละตินว่า Aurum หมายถึงทองคำ"));

        questionBank.add(new Question(203L, "GENERAL", "EASY",
                "ดาวเคราะห์ดวงใดได้รับฉายาว่า 'ดาวเคราะห์แดง'?",
                List.of("ดาวพุธ", "ดาวศุกร์", "ดาวอังคาร", "ดาวพฤหัสบดี"), 2,
                "ดาวอังคารมีลักษณะเป็นสีแดงจากแร่เหล็กที่เกิดออกไซด์บนพื้นผิว"));

        questionBank.add(new Question(204L, "GENERAL", "EASY",
                "กำแพงเมืองจีนถูกสร้างขึ้นในอดีตเพื่อวัตถุประสงค์หลักใด?",
                List.of("กั้นเขตการค้า", "ป้องกันการรุกรานจากชนเผ่าทางเหนือ", "เป็นเส้นทางขนส่งสินค้า", "สร้างเป็นที่ระลึก"), 1,
                "กำแพงและแนวป้องกันถูกสร้างและขยายเพื่อช่วยป้องกันการรุกรานจากกลุ่มชนทางตอนเหนือ"));

        questionBank.add(new Question(205L, "GENERAL", "MEDIUM",
                "ดาวเคราะห์ดวงใดเคยได้รับการยืนยันว่ามีดวงจันทร์บริวารมากที่สุดในช่วงเวลาปัจจุบันของข้อมูลชุดนี้?",
                List.of("ดาวพฤหัสบดี", "ดาวเสาร์", "ดาวอังคาร", "ดาวเนปจูน"), 1,
                "ดาวเสาร์มีระบบดวงจันทร์จำนวนมากและเคยทำสถิติเป็นดาวเคราะห์ที่มีดวงจันทร์ยืนยันมากที่สุด"));

        questionBank.add(new Question(206L, "GENERAL", "MEDIUM",
                "อวัยวะใดทำหน้าที่หลักในการผลิตฮอร์โมนอินซูลิน?",
                List.of("ตับ (Liver)", "ไต (Kidney)", "ตับอ่อน (Pancreas)", "ถุงน้ำดี (Gallbladder)"), 2,
                "ตับอ่อน โดยเฉพาะเบต้าเซลล์ใน Islets of Langerhans เป็นแหล่งผลิตอินซูลิน"));

        questionBank.add(new Question(207L, "GENERAL", "MEDIUM",
                "ชาติใดส่งมนุษย์ขึ้นสู่อวกาศสำเร็จเป็นชาติแรก?",
                List.of("สหรัฐอเมริกา", "สหภาพโซเวียต", "จีน", "สหราชอาณาจักร"), 1,
                "สหภาพโซเวียตส่ง Yuri Gagarin ขึ้นสู่อวกาศในปี 1961"));

        questionBank.add(new Question(208L, "GENERAL", "MEDIUM",
                "หน่วยวัดความถี่ในระบบ SI คืออะไร?",
                List.of("จูล (Joule)", "วัตต์ (Watt)", "เฮิรตซ์ (Hertz)", "พาสคัล (Pascal)"), 2,
                "เฮิรตซ์ (Hz) คือหน่วย SI สำหรับความถี่ เท่ากับหนึ่งรอบต่อวินาที"));

        questionBank.add(new Question(209L, "GENERAL", "HARD",
                "ก๊าซชนิดใดมีสัดส่วนมากที่สุดในชั้นบรรยากาศของโลก?",
                List.of("ออกซิเจน (Oxygen)", "ไนโตรเจน (Nitrogen)", "คาร์บอนไดออกไซด์ (CO2)", "อาร์กอน (Argon)"), 1,
                "ไนโตรเจนคิดเป็นสัดส่วนประมาณ 78% ของบรรยากาศโลก"));

        questionBank.add(new Question(210L, "GENERAL", "HARD",
                "สนธิสัญญาใดเป็นข้อตกลงสำคัญที่ยุติสงครามระหว่างเยอรมนีกับฝ่ายสัมพันธมิตรหลังสงครามโลกครั้งที่ 1?",
                List.of("สนธิสัญญาเวสต์ฟาเลีย", "สนธิสัญญาแวร์ซาย", "สนธิสัญญากรุงเจนีวา", "สนธิสัญญาปารีส"), 1,
                "Treaty of Versailles ลงนามในปี 1919 และกำหนดเงื่อนไขสันติภาพต่อเยอรมนี"));

        questionBank.add(new Question(211L, "GENERAL", "HARD",
                "ภาพวาด Mona Lisa จัดแสดงอยู่ที่พิพิธภัณฑ์ใด?",
                List.of("The British Museum", "The Louvre Museum", "The Met Museum", "Uffizi Gallery"), 1,
                "Mona Lisa ของ Leonardo da Vinci จัดแสดงที่พิพิธภัณฑ์ลูฟร์ในกรุงปารีส"));

        questionBank.add(new Question(212L, "GENERAL", "HARD",
                "นักคณิตศาสตร์ชาวอังกฤษผู้มีบทบาทสำคัญต่อแนวคิดเครื่องจักรคำนวณเชิงทฤษฎีและวิทยาการคอมพิวเตอร์คือใคร?",
                List.of("Charles Babbage", "Alan Turing", "John von Neumann", "Ada Lovelace"), 1,
                "Alan Turing เสนอแนวคิด Turing Machine ซึ่งกลายเป็นรากฐานสำคัญของทฤษฎีการคำนวณ"));
    }

    public List<QuestionDTO> getQuestions(String category, String difficulty, int limit) {
        int safeLimit = Math.max(MIN_LIMIT, Math.min(limit, MAX_LIMIT));

        List<Question> filtered = filterQuestions(category, difficulty);
        Collections.shuffle(filtered);

        return filtered.stream()
                .limit(safeLimit)
                .map(q -> new QuestionDTO(q.getId(), q.getCategory(), q.getDifficulty(), q.getQuestionText(), q.getOptions()))
                .collect(Collectors.toList());
    }

    /** Number of questions that match the filters (before the per-quiz limit is applied). */
    public int countQuestions(String category, String difficulty) {
        return filterQuestions(category, difficulty).size();
    }

    /** Largest quiz length that can actually be served for the given filters. */
    public int maxLimitFor(String category, String difficulty) {
        return Math.min(countQuestions(category, difficulty), MAX_LIMIT);
    }

    private List<Question> filterQuestions(String category, String difficulty) {
        String safeCategory = normalize(category);
        String safeDifficulty = normalize(difficulty);

        return questionBank.stream()
                .filter(q -> "ALL".equals(safeCategory) || q.getCategory().equalsIgnoreCase(safeCategory))
                .filter(q -> "ALL".equals(safeDifficulty) || q.getDifficulty().equalsIgnoreCase(safeDifficulty))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public QuizResultResponse evaluateQuiz(QuizRequest request) {
        Map<Long, Integer> userAnswers = request != null && request.getUserAnswers() != null
                ? request.getUserAnswers()
                : new LinkedHashMap<>();

        List<QuestionReviewDTO> reviews = new ArrayList<>();
        int correctCount = 0;
        int totalScore = 0;

        for (Map.Entry<Long, Integer> entry : userAnswers.entrySet()) {
            Question q = questionById.get(entry.getKey());
            if (q == null) {
                continue;
            }

            Integer userChoice = entry.getValue();
            boolean isCorrect = userChoice != null && userChoice >= 0 && userChoice == q.getCorrectIndex();

            if (isCorrect) {
                correctCount++;
                totalScore += pointsFor(q.getDifficulty());
            }

            reviews.add(new QuestionReviewDTO(
                    q.getId(),
                    q.getQuestionText(),
                    q.getOptions(),
                    userChoice == null ? -1 : userChoice,
                    q.getCorrectIndex(),
                    isCorrect,
                    q.getExplanation()
            ));
        }

        int totalQuestions = reviews.size();
        double accuracy = totalQuestions == 0 ? 0.0 : (correctCount * 100.0) / totalQuestions;
        String grade = gradeFor(accuracy);

        return new QuizResultResponse(totalQuestions, correctCount, totalScore, accuracy, grade, reviews);
    }

    private int pointsFor(String difficulty) {
        return switch (normalize(difficulty)) {
            case "HARD" -> 200;
            case "MEDIUM" -> 150;
            default -> 100;
        };
    }

    private String gradeFor(double accuracy) {
        if (accuracy >= 90) return "S";
        if (accuracy >= 80) return "A";
        if (accuracy >= 70) return "B";
        if (accuracy >= 60) return "C";
        return "D";
    }

    private String normalize(String value) {
        return Optional.ofNullable(value)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toUpperCase)
                .orElse("ALL");
    }
}
