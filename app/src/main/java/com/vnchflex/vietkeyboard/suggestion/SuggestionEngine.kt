package com.vnchflex.vietkeyboard.suggestion

import android.content.Context
import com.vnchflex.vietkeyboard.engine.DiacriticUtils

/**
 * Modern Vietnamese Word Suggestion & Trigram Next-Word Suggestion Engine
 * (Architecture aligned with Modern-Vietnamese-IME-and-Macro-Engine & Gboard):
 *
 * 1. **Word Suggestions (Prefix + Unaccented-to-Accented + Context Re-ranking)**:
 *    - Matches exact accented prefix AND unaccented Telex/ASCII prefix (`"viet"` -> `"việt"`, `"dzia"` -> `"dzìa"`, `"dzach"` -> `"dzách"`).
 *    - Re-ranks prefix candidates using active Trigram `(prev2, prev1)` and Bigram `(prev1)` language model weights
 *      combined with unigram frequency and user-learned frequency.
 *    - Integrates Personal Dictionary shortcuts, Macro expansion previews, and Emoji suggestions.
 *
 * 2. **Trigram Next-Word Suggestions (`P(w_i | w_{i-2}, w_{i-1})` with Katz Backoff)**:
 *    - Primary: User-learned Trigrams + Bundled Vietnamese Trigram table `(prev2, prev1) -> [candidates]`.
 *    - Backoff 1: User-learned Bigrams + Bundled Bigram table `(prev1) -> [candidates]`.
 *    - Backoff 2: High-frequency Vietnamese sentence starters / unigrams.
 *
 * 3. **Online N-gram Learning**:
 *    - Automatically learns Unigrams, Bigrams `(prev1 -> word)`, and Trigrams `(prev2 + " " + prev1 -> word)`
 *      as the user commits words, persisting them locally via `PersonalDictionaryStore`.
 */
class SuggestionEngine(context: Context? = null) {
    private val dictStore = context?.let { PersonalDictionaryStore(it) }
    private val learnLock = Any()
    private val trainedData = loadTrainedDataAsset(context)

    // Unigram dictionary with base frequency weights (including Southern Vietnamese dialect words: dzìa, dzách, dzô, dzậy, dzọt, dzui...)
    private val coreUnigramWeights: Map<String, Int> = buildMap {
        val highFreq = mapOf(
            "xin" to 95, "chào" to 96, "việt" to 98, "nam" to 97, "bàn" to 88, "phím" to 89,
            "không" to 100, "được" to 99, "cảm" to 94, "ơn" to 94, "tôi" to 96, "bạn" to 97,
            "đang" to 92, "làm" to 93, "gì" to 91, "hôm" to 90, "nay" to 92, "ngày" to 91,
            "mai" to 89, "đi" to 93, "học" to 88, "việc" to 90, "nhà" to 91, "công" to 92,
            "nghệ" to 87, "android" to 85, "macro" to 82, "bỏ" to 84, "dấu" to 85, "tự" to 88,
            "do" to 86, "thương" to 87, "trường" to 88, "người" to 95, "những" to 93, "trong" to 94,
            "ngoài" to 86, "thời" to 90, "gian" to 90, "thành" to 91, "phố" to 91, "hà" to 92,
            "nội" to 92, "sài" to 93, "gòn" to 93, "đà" to 89, "nẵng" to 89, "chúng" to 90,
            "ta" to 89, "mình" to 95, "nhưng" to 91, "với" to 93, "cho" to 94, "biết" to 92,
            "thế" to 90, "nào" to 91, "bây" to 89, "giờ" to 92, "tất" to 88, "cả" to 90,
            "rất" to 91, "vui" to 90, "nhiều" to 92, "lắm" to 89, "quá" to 90, "luôn" to 89,
            "nha" to 91, "nhé" to 91, "ạ" to 90, "ơi" to 89, "rồi" to 94, "chưa" to 92,
            "có" to 98, "là" to 98, "của" to 96, "và" to 97, "này" to 93, "đó" to 92,
            "anh" to 94, "em" to 94, "chị" to 90, "ăn" to 89, "cơm" to 87, "uống" to 86,
            "nước" to 90, "cà" to 86, "phê" to 86, "điện" to 89, "thoại" to 89, "máy" to 88,
            "tính" to 88, "chương" to 85, "trình" to 86, "ứng" to 87, "dụng" to 87, "hệ" to 86,
            "thống" to 86, "tiếng" to 93, "nhanh" to 88, "chóng" to 84, "tốt" to 90,
            "đẹp" to 88, "mới" to 91, "cũ" to 83, "muốn" to 92, "cần" to 91, "phải" to 92,
            // Từ phương ngữ Nam Bộ & Gen-Z (vnchKey)
            "dzìa" to 88, "dzách" to 87, "dzô" to 89, "dzậy" to 88, "dzọt" to 85, "dzui" to 87,
            "dzữ" to 85, "dza" to 82, "zậy" to 86, "zui" to 86, "wá" to 85, "fải" to 84,
            "bá" to 85, "cháy" to 84, "thiệt" to 88, "hông" to 88, "hen" to 85, "nghen" to 85,
        )
        putAll(highFreq)

        // Bộ từ vựng tiếng Việt phổ thông mở rộng (~1.100 từ đơn & âm tiết thông dụng cho Glide Typing & Gợi ý từ)
        val commonVietnameseWords = """
            ai an anh áo âm ấm ấn ẩn ấp âu ấy ba bà bá bả bã
            bạ bác bạc bách bài bán bạn bàn bảng bánh bao báo bảo bão bạo bay
            bảy bắc bắt bận bằng bắp bầu bé bẻ bẽ bẹ bên bến bệnh bi bì
            bí bỉ bị bia bịa biến biển biết biệt biểu binh bình bính bò bỏ bó
            bọ bói bọc bóc bọn bóng bỏng bỗng bộ bốc bôi bốn bộn bông bồng bớt
            bơi bởi bơm bỡn bợn buồn buôn buông buổi buộc bước bưởi bướm bưu ca cà
            cá cả cạ các cạc cách cai cái cải cãi cạm cam cảm cám cạn can
            cần cân cấn cẩn cận căng cắt cằm cắn cặn căn cặp câu cầu cấu cậu
            cây cầy cậy cha chà chá chả chạ chác chạc chai chài trái chải chạy cháy
            chày chắc chăm chằm chắn chặn chăng chẳng chặng chân trần chần chấn chẩn trận chập
            chất châu chầu chậu chê chế chệ chết chi chì chí chỉ chị chia chìa chĩa
            chiếc chiêm chiếm chiến chiều chiếu chim chìm chín chỉnh chính chịu cho trò chó chỏ
            chọ chói chọn chốn chồng chổng chống chớp chờ chở chợ chơi chới chôn chỗ chốc
            chồi chối chổi trộn trông trống chờn chớt chua chùa chúa chủa chung chùng chúng chủng
            chuối chuỗi chuồn chuông chuộng chuột chụp chút chuyên chuyền chuyển chuyện chữ chữa chưa chừa
            chứa chửa chương trường chưởng có cò cỏ cọ coi cõi cọc con còn cỏn cọn
            cong còng cóng cổng cộng cố cộ cốc cộc côi cối cội cốm cồn côn cống
            công cồng cốt cột cơ cờ cớ cỡ cợ cơn cợn cởi cưới cười cưỡi cướp
            cước cược cứu cựu da dà dá dả dạ dài dải dãi dại dám dạm dan
            dàn dán dạn dang dàng dáng dạng danh dành dánh dạnh dao dào dáo dạo dạy
            dãy dày dầy dây dậy dẫn dận dâng dập dâu dầu dấu dậu dê dề dế
            dễ dệ dệt di dì dí dỉ dị dĩa dịch diễn diện diều diệu dinh dình
            dính dịnh do dò dó dỏ dọ dọa dọn dòng dóng dọng dỗ dốc dồi dối
            dội dồn dông giống dốt dột dơ dờ dớ dở dợ đời đới đợi đơm đờn
            đợt du dù dú dủ dụ dua dùa dúa dũa dùng dũng dụng duỗi đuổi đuốiar
            đuôi đút duy duyên duyệt dư dừ dứ dữ dự dưa dừa dứa dựa dương dường
            dứt đa đà đá đả đã đạ đạc đai đài đái đải đãi đại đàm đảm
            đám đạm đan đàn đán đạn đang đàng đáng đãng đạng đanh đành đánh đạo đạp
            đạt đau đàu đáu đảu đãu đạu đắc đằm đắm đặm đăn đằn đắn đặn đăng
            đằng đắng đẳng đặng đắp đặt đắt đầu đấu đậu đây đầy đấy đậy đâm đầm
            đấm đậm đân đần đấn đận đấp đập đất đâu đê đề đế để đễ đệ
            đêm đềm đếm đệm đên đền đến đện đênh đềnh đết đi đì đí đỉ đĩ
            đị đia đìa đĩa địa đích địch điếc điềm điểm điếm điệm điên điền điển điện
            điêng điềng điếng điệp điều điểu điệu đinh đình đính đỉnh định đo đò đó đỏ
            đõ đọ đoa đoà đoá đoả đoã đoạ đoan đoàn đoán đoản đoạn đoạt đọc đóc
            đỏi đói đọi đom đóm đòm đon đòn đón đọn đong đòng đóng đỏng đọng đô
            đồ đố đổ đỗ độ đốc độc đôi đồi đối đổi đỗi đội đôm đồm đốm
            độm đôn đồn đốn độn đông đồng đống đổng động đốt đột đơ đờ đớ đỡ
            đợ đơi đởi đỡi đờm đớm đợm đơn đớn đởn đợn đớp đu đù đú đủ
            đũ đụ đua đùa đúa đủa đũa đụa đúc đục đùi đúi đủi đũi đụi đùm
            đúm đụm đun đùn đún đụn đung đùng đúng đủng đụng đuốc đuồi đuối đuỗi đuội
            đụt đưa đừa đứa đửa đữa đựa đức đười đươi đượm đương đường đướng đưởng đưỡng
            đượng đứt đựt em èm ém ẻm ẽm ẹm en èn én ẻn ẽn ẹn eng
            èng éng ẻng ẽng ẹng eo èo éo ẻo ẽo ẹo ép ẹp êt ệt êm
            ềm ếm ểm ễm ệm ên ền ến ển ễn ện ênh ềnh ếnh ểnh ễnh
            ệnh ếp ệp ết ều ếu ểu ễu ệu ga gà gá gả gã gạ gác
            gạc gạch gai gài gái gải gãi gại gan gàn gán gản gãn gạn gang gàng
            gáng gảng gãng gạng ganh gành gánh gảnh gãnh gạnh gao gào gáo gảo gão gạo
            gay gày gáy gảy gãy gạy gắc gặc găm gằm gắm gẳm gẵm gặm găn gằn
            gắn gẳn gẵn gặn găng gằng gắng gẳng gẵng gặng gặp gắp gắt gặt gâu gầu
            gấu gẩu gẫu gậu gây gầy gấy gẩy gẫy gậy gâm gầm gấm gẩm gẫm gậm
            gần gẩn gẫn gận gấp gập gấc gật ghê nghề ghế ghể ghễ ghệ ghềnh ghép
            ghét ghi ghì ghí ghỉ ghĩ ghị ghim ghìm gia già giá giả giã giạ giác
            giạc giặc giai giài giái giải giãi giại giam giàm giám giảm giãm giạm gian giàn
            gián giản giãn giạn giang giàng giáng giảng giãng giạng gianh giành giánh mảnh giao giào
            giáo giảo gião giạo giăm giằm giắm giẳm giẵm giặm giăng giằng giắng giẳng giẵng giặng
            giáp giặt giàu giáu giảu giãu giạu giày giáy giãy giạy giâm giầm giấm giẩm giẫm
            giậm giân dần giấn giẫn giận giấp giập giấc giật giâu giầu giấu giẩu giẫu giậu
            giây giầy giấy giẩy giẫy giậy giêng giềng giếng giết giễu giò gió giỏ rõ giọ
            gióc giọc giỏi giói giọi giòn gióng giỏng giọng giọt giờ giới giữ giữa giương giường
            giướng giưởng giưỡng giượng giựt go gò gó gỏ gõ gọ góc gọc gọi gom gòm
            góm gỏm gõm gọm gon gòn gón gỏn gõn gọn gong gòng góng gỏng gõng gọng
            gô gồ gổ gỗ gộ gốc gộc gôi gồi gối gổi gỗi gội gồm gốm gộm
            gôn gồn gốn gổn gỗn gộn gông gồng gổng gỗng gộng gốt gột gơ gờ gớ
            gở gỡ gợ gơi gời gới gởi gợi gớm gợm gơn gờn gớn gởn gỡn gợn
            góp gộp gót gọt gu gù gú gủ gũ gụ gục gùi gúi gủi gũi gụi
            gùm gúm gụm gùn gún gụn gùng gúng gủng gũng gụng gút gụt gương gường gướng
            gưởng gưỡng gượng gườm gượm gửi ha hà há hả hã hạ hác hạc hạch hai
            hài hái hải hãi hại ham hàm hám hảm hãm hạm han hàn hán hản hãn
            hạn hang hàng háng hảng hãng hạng hanh hành hánh hảnh hãnh hạnh hao hào háo
            hảo hão hạo hay hày háy hảy hãy nhạy hắc hặc hăm hằm hắm hẳm hẵm
            hặm hắn hẳn hẵn hặn hăng hằng hắng hẳng hẵng hặng hắp hặp hắt hặt hâu
            hầu hấu hẩu hẫu hậu hây hầy hấy hẩy hẫy hậy hâm hầm hấm hẩm hẫm
            hậm hân hần hấn hẫn hận hấp hập hấc hật he hè hé hẻ hẽ hẹ
            hem hèm hém hẻm hẽm hẹm hen hèn hén hẻn hẽn hẹn heng hèng héng hẻng
            hẽng hẹng heo hèo héo hẻo hẽo hẹo hép hẹp hêt hệt hê hề hế hệ
            hêm hềm hếm hểm hễm hệm hên hền hến hển hễn hện hênh hềnh hếnh hểnh
            hễnh hệnh hếp hệp hết hêu hếu hểu hễu hi hì hí hỉ hĩ hị hia
            hìa hía hỉa hĩa hịa hích hịch hiếm hiểm hiềm hiện hiền hiển hiến hiêng hiềng
            hiếng hiệp hiểu hiệu hinh hình hính hỉnh hĩnh hịnh ho hò hó hỏ hõ họ
            hoa hoà hoá hoả hoã hoạ hoác hoặc hoài hoải hoại hoan hoàn hoán hoản hoãn
            hoạn hoang hoàng hoáng hoảng hoãng hoạng hoành hoánh hoảnh hoãnh hoạnh hoạt hoắt học hóc
            hỏi hói họi hom hòm hóm hỏm hõm họp hon hòn hón hỏn hõn họn hong
            hòng hóng hỏng hõng họng hô hồ hố hổ hỗ hộ hốc hộc hôi hồi hối
            hội hôm hốm hổm hỗm hộm hôn hồn hốn hổn hỗn hông hồng hống hỗng hộng
            hốt hột hơ hờ hớ hở hỡ hợ hơi hời hới hởi hỡi hợi hơm hờm
            hớm hởm hỡm hợp hơn hờn hớn hởn hỡn hợn hớp hớt hợt hu hù hú
            hủ hũ hụ hua hùa hứa hủa hũa hụa húc hục hui hùi húi hủi hũi
            hụi hum hùm húm hủm hũm hụm hun hùn hún hủn hũn hụn hung hùng húng
            hủng hũng hụng huơ huờ huớ huở huỡ huợ húp hụp hút hụt huy huỳ huý
            huỷ huỹ huỵ huých huỵch huyên huyền khuyến huyển huyễn huyện huynh huỳnh huýnh huỷnh huỹnh
            huỵnh huyết huyệt hư hử hữ hự hưa hừa hửa hữa hựa hức hực hươi hười
            hưới hưởi hưỡi hượi hươm hườm hướm hưởm hưỡm hượm hươn hườn hướn hưởn hưỡn hượn
            hương hường hướng hưởng hưỡng hượng hươu hườu hướu hưởu hưỡu hượu hứt hựt hy hỳ
            hý hỷ hỹ hỵ ích ịch im ìm ím ỉm ĩm ịm in ìn ín ỉn
            ĩn ịn inh ình ính ỉnh ĩnh ịnh ít ịt iu ìu íu ỉu ĩu ịu
            khả khách khám khóc khỏe khỏi khói không khu khác khát khâu khăn khắp khắt khe
            khẽ khen khéo khép khét khi khỉ khí khía khích khiến khiêng khiếp khiếu khinh khó
            khoá khoác khoai khoái khoan khoản khoảng khoanh khoẻ khoe khoét khom khóm khôn khốn khổng
            khô khổ khố khối khôi khơi khớp khù khú khủ khũ khụ khua khuân khuất khúc
            khuếch khung khủng khuyên khuyết khuynh khuya khử khứ khứa khước khướt khứu kia kìa kía
            kỉa kĩa kịa kịch kiếm kiểm kiềm kiện kiền kiến kiêng kiềng kiếng kiếp kịp kêu
            kều kéo kẻo kẹo kèm kém kẽm kèn kén kẻn kẽn kẹn keo kèo kẽo kép
            kẹp kết kệt kê kề kế kể kễ kệ kênh kềnh kếnh kểnh kễnh kệnh kếu
            kiểu kễu kệu khạc khai khái khải khãi khại kham khàm khảm khãm khạm khan khàn
            khán khản khãn khạn khang khàng kháng khẳng khãng khạng khanh khành khánh khảnh khãnh khạnh
            khao khào kháo khảo khão khạo khay khày kháy khảy khãy khạy khắc khằm khắm khẳm
            khẵm khặm khằn khắn khẳn khẵn khặn khăng khằng khắng khẵng khặng khặp khặt khầu khấu
            khẩu khẫu khậu khây khầy khấy khẫy khậy khâm khầm khấm khẩm khẫm khậm khân khần
            khấn khẩn khẫn khận khật khiêm khiếm khiểm khiễm kiệm kiên kiển kiễn kiểng kiễng kiệu
            kim kìm kím kỉm kĩm kịm kin kìn kín kỉn kĩn kịn kinh kình kính kỉnh
            kĩnh kịnh kíp kít kịt kìu kíu kỉu kĩu kịu kỹ kỷ kỳ ký kỵ la
            là lá lả lã lạ lác lạc lách lai lài lái lải lãi lại lam làm
            lám lảm lãm lạm lan làn lán lản lãn lạn lang làng láng lảng lãng lạng
            lanh lành lánh lảnh lãnh lạnh lao lào láo lảo lão lạo lay lày láy lảy
            lãy lạy lắc lặc lăm lằm lắm lẳm lẵm lặm lăn lằn lắn lẳn lẵn lặn
            lăng lằng lắng lẳng lẵng lặng lắp lặp lắt lặt lâu lầu lấu lẩu lẫu lậu
            lây lầy lấy lẩy lẫy lậy lâm lầm lấm lẩm lẫm lậm lân lần lấn lẩn
            lẫn lận lấp lập lấc lật lê lề lế lể lễ lệ lêm lềm lếm lểm
            lễm lệm lên lền lến lển lễnh lệnh lênh lềnh lếnh lểnh lếp lệp lết lệt
            lêu lều lếu lểu lễu lệu li lì lí lỉ lĩ lị lia lìa lía lỉa
            lĩa lịa lích lịch liêm liềm liếm liểm liễm liệm liên liền liến liển liễn liện
            liêng liềng liếng liểng liễng liệng liếp liệp liết liệt liêu liều liếu liểu liễu liệu
            lim lìm lím lỉm lĩm lịm lin lìn lín lỉn lĩn lịn linh lình lính lỉnh
            lĩnh lịnh líp dịp lít lịt liu lìu líu lỉu lĩu lịu lo lò ló lỏ
            lõ lọ loa loà loá loả loã loạ loạc loài loái loải loãi loại loan loàn
            loán loản loãn loạn loang loàng loáng loảng loãng loạng loanh loành loánh loảnh loãnh loạnh
            loát loạt loắt loặt lọc lóc lỏi lói lọi lom lòm lóm lỏm lõm lọm lon
            lòn lón lỏn lõn lọn long lòng lóng lỏng lõng lọng lô lồ lố lổ lỗ
            lộ lốc lộc lôi lồi lối lổi lỗi lội lôm lồm lốm lổm lỗm lộm lôn
            lồn lốn lổn lỗn lộn lông lồng lống lổng lỗng lộng lốt lột lơ lờ lớ
            lở lỡ lợ lơi lời lới lởi lỡi lợi lơm lờm lớm lởm lỡm lợm lơn
            lờn lớn lởn lỡn lợn lớp lợp lớt lợt lu lù lú lủ lũ lụ lua
            lùa lúa lủa lũa lụa lúc lục lui lùi lúi lủi lũi lụi lum lùm lúm
            lủm lũm lụm lun lùn lún lủn lũn lụn lung lùng lúng lủng lũng lụng luộc
            luôi luồi luối luổi luỗi luội lúp lụp lút lụt luy luỳ luý luỷ luỹ luỵ
            luyên luyền luyến luỷen luyễn luyện luynh luỳnh luýnh luỷnh luỹnh luỵnh lư lừ lứ lử
            lữ lự lưa lừa lứa lửa lữa lựa lức lực lươi lười lưới lưởi lưỡi lượi
            lươm lườm lướm lưởm lưỡm lượm lươn lườn lướn lưởn lưỡn lượn lương lường nướng lưởng
            lưỡng lượng lướt lượt lưu lừu lứu lửu lữu lựu lứt lựt ly lỳ lý lỷ
            lỹ lỵ ma mà má mả mã mạ mác mạc mạch mai mài mái mải mãi
            mại mam màm mám mảm mãm mạm man màn mán mản mãn mạn mang màng máng
            mảng mãng mạng manh mành mánh mãnh mạnh mao mào máo mảo mão mạo may mày
            máy mảy mãy mạy mắc mặc măm mằm mắm mẳm mẵm mặm măn mằn mắn mẳn
            mẵn mặn măng mằng mắng mẳng mẵng mặng mặt mắt mâu mầu mấu mẩu mẫu mậu
            mây mầy mấy mẩy mẫy mậy mâm mầm nấm mẩm mẫm mậm mân mần mấn mẩn
            mẫn mận mấp mật mê mề mế mể mễ mệ mêm mềm mếm mểm mễm mệm
            mên mền mến mển mễn mện mênh mềnh mếnh mểnh mễnh mệnh mếp mệp mết mệt
            mêu mều mếu mểu mễu mệu mi mì mí mỉ mĩ mị mia mìa mía mỉa
            mĩa mịa mích mịch miên miền miến miễn miệng miếng miết miệt miêu miều miếu miểu
            miễu miệu mim mìm mím mỉm mĩm mịm min mìn mín mỉn mĩn mịn minh mình
            mính mỉnh mĩnh mịnh míp mịp mít mịt miu mìu míu mỉu mĩu mịu mo mò
            mó mỏ mõ mọ moa moà moá moả moã moạ móc mọc mỏi mói mọi mom
            mòm móm mỏm mõm mọm mon mòn món mỏn mõn mọn mong mòng móng mỏng mõng
            mọng mô mồ mố mổ mỗ mộ mốc mộc môi mồi mối mỗi mội môm mồm
            mốm mổm mỗm mộm môn mồn muốn mổn mỗn một mông mồng mống mỗng mộng mốt
            mơ mờ mớ mở mỡ nợ mơi mời mới mơm mờm mớm mởm mỡm mợm mơn
            mờn mớn mởn mỡn mợn mớp mợp mớt mượt mu mù mú mủ mũ mụ mua
            mùa múa mủa mũa mụa múc mục mui mùi múi mủi mũi mụi mum mùm múm
            mủm mũm mụm mun mùn mún mủn mũn mụn mung mùng múng mủng mũng mụng muôi
            muồi muối muổi muỗi muội muôn muồn muổn muỗn muộn muỗng múp mụp mút mụt mưa
            mừa mứa mửa mữa mựa mức mực mươi mười mưới mưởi mưỡi mượi mươn mườn mướn
            mưởn mưỡn mượn mương mường mưởng mưỡng mượng mướt mưu mừu mứu mửu mữu mựu mứt
            mựt my mỳ mỹ mỷ mỵ na nà ná nả nã nạ nác nạc nách nai
            nài nái nải nãi nại nam nàm nám nảm nãm nạm nan nàn nán nản nãn
            nạn nang nàng náng nảng nãng nạng nanh nành nánh nảnh nãnh nạnh nao nào náo
            nảo não nạo nay này náy nảy nãy nạy nắc nặc năm nằm nắm nẳm nẵm
            nặm năn nằn nắn nẳn nẵn nặn năng nằng nắng nẳng nẵng nặng nắp nặp nắt
            nặt nâu nầu nấu nẩu nẫu nậu nây nầy nấy nẩy nẫy nậy nẩm nẫm nậm
            nấn nẩn nẫn nận nấp nập nấc nật nê nề nế nể nễ nệ nêm nềm
            nếm nểm nễm nệm nên nền nến nển nễn nện nênh nềnh nếnh nểnh nễnh nệnh
            nếp nệp nết nệt nêu nều nếu nểu nễu nệu ni nì ní nỉ nĩ nị
            nia nìa nía nỉa nĩa nịa ních nịch niêm niềm niếm niểm niễm niệm niên niền
            niến niển niễn niện niêng niềng niếng niểng niễng niệng niết niệt niêu niều niếu niểu
            niễu niệu nim nìm ním nỉm nĩm nịm nin nìn nín nỉn nĩn nịn ninh nình
            nính nỉnh nĩnh nịnh níp nịp nít nịt niu nìu níu nỉu nĩu nịu no nò
            nó nỏ nõ nọ noa noà noá noả noã noạ noãn nọc nóc nỏi nói nọi
            nom nòm nóm nỏm nõm nọm non nòn nón nỏn nõn nọn nong nòng nóng nỏng
            nõng nọng nô nồ nố nổ nỗ nộ nốc nộp nôi nồi nối nổi nỗi nội
            nôm nồm nốm nổm nỗm nộm nôn nồn nốn nổn nỗn nộn nông nồng nống nổng
            nỗng nộng nốt nột nơ nờ nớ nở nỡ nơi nời nới nởi nỡi nợi nơm
            nờm nớm nởm nỡm nợm nơn nờn nớn nởn nỡn nợn nớp nớt nợt nu nù
            nú nủ nũ nụ nua nùa núa nủa nũa nụa núc nục nui nùi núi nủi
            nũi nụi num nùm núm nủm nũm nụm nun nùn nún nủn nũn nụn nung nùng
            núng nủng nũng nụng nuôi nuồi nuối nuổi nuỗi nuội nuốt nuột núp nụp nút nụt
            nưa nừa nứa nửa nữa nựa nức nực nươi nười nưới nưởi nưỡi nượi nươm nườm
            nướm nưởm nưỡm nượm nươn nườn nướn nưởn nưỡn nượn nương nường nưởng nưỡng nượng nước
            nược nướt nượt nứt nựt nữ nha nhà nhá nhả nhã nhạ nhác nhạc nhách nhai
            nhài nhái nhải nhãi nhại nham nhàm nhám nhảm nhãm nhạm nhan nhàn nhán nhản nhãn
            nhạn nhang nhàng nháng nhảng nhãng nhạng nhanh nhành nhánh nhảnh nhãnh nhạnh nhao nhào nháo
            nhảo nhão nhạo nhay nhày nháy nhảy nhãy nhắc nhặc nhăm nhằm nhắm nhẳm nhẵm nhặm
            nhăn nhằn nhắn nhẳn nhẵn nhặn nhăng nhằng nhắng nhẳng nhẵng nhặng nhắp nhặp nhắt nhặt
            nhau nhàu nháu nhảu nhãu nhạu nhây nhầy nhấy nhẫy nhậy nhâm nhầm nhấm nhẩm nhẫm
            nhậm nhân nhần nhấn nhẩn nhẫn nhận nhấp nhập nhấc nhật nhê nhề nhế nhể nhễ
            nhệ nhêm nhềm nhếm nhểm nhễm nhiệm nhên nhền nhến nhển nhễn nhện nhênh nhềnh nhếnh
            nhểnh nhễnh nhệnh nhếp nhệp nhết nhiệt nhêu nhều nhếu nhểu nhễu nhệu nhi nhì nhí
            nhỉ nhĩ nhị nhia nhìa nhía nhỉa nhĩa nhịa nhích nhịch nhiếc nhiềm nhiễm nhiên nhiền
            nhiến nhiển nhiễn nhiện nhiếp nhiệp nhiết nhiêu nhiều nhiếu nhiểu nhiễu nhiệu nhim nhìm nhím
            nhỉm nhĩm nhịm nhin nhìn nhín nhỉn nhĩn nhịn nhinh nhình nhính nhỉnh nhĩnh nhịnh nhíp
            nhịp nhít nhịt nhiu nhìu nhíu nhỉu nhĩu nhịu nho nhò nhó nhỏ nhõ nhọ nhoa
            nhoà nhoá nhoả nhoã nhoạ nhoài nhoải nhoại nhoáng nhoạng nhóc nhọc nhói nhỏi nhọi nhom
            nhòm nhóm nhỏm nhõm nhọm nhon nhòn nhón nhỏn nhõn nhọn nhong nhòng nhóng nhỏng nhõng
            nhọng nhô nhồ nhố nhổ nhỗ nhộ nhốc nhộc nhôi nhồi nhối nhổi nhỗi nhội nhôm
            nhồm nhốm nhổm nhỗm nhộm nhôn nhồn nhốn nhổn nhỗn nhộn nhông nhồng nhống nhổng nhỗng
            nhộng nhốt nhột nhơ nhờ nhớ nhở nhỡ nhợ nhơi nhời nhởi nhỡi nhợi nhơm nhờm
            nhớm nhởm nhỡm nhợm nhơn nhờn nhớn nhởn nhỡn nhợn nhớp nhợp nhớt nhợt nhu nhù
            nhú nhủ nhũ nhụ nhua nhùa nhúa nhủa nhũa nhụa nhúc nhục nhui nhùi nhúi nhủi
            nhũi nhụi nhum nhùm nhúm nhủm nhũm nhụm nhun nhùn nhún nhủn nhũn nhụn nhung nhùng
            nhúng nhủng nhũng nhụng nhuốc nhuôi nhuồi nhuối nhuổi nhuỗi nhuội nhuộm nhuần nhuẩn nhuẫn nhuận
            nhúp nhụp nhút nhụt như nhừ nhứ nhử nhữ nhự nhưa nhừa nhứa nhửa nhữa nhựa
            nhức nhực nhươi nhười nhưới nhưởi nhưỡi nhượi nhươm nhườm nhưởm nhưỡm nhượm nhươn nhườn nhưởn
            nhưỡn nhượn nhương nhường nhướng nhưởng nhưỡng nhượng nhước nhược nhướt nhượt nhứt nhựt qua quà
            quá quả quã quạ quan quàn quán quản quãn quạn quang quàng quáng quảng quãng quạng
            quanh quành quánh quảnh quãnh quạnh quay quày quáy quảy quãy quạy quắc quằm quắm quẳm
            quẵm quặm quăn quằn quắn quẳn quẵn quặn quăng quằng quắng quẳng quẵng quặng quắp quặp
            quắt quặt quân quần quấn quẩn quẫn quận quất quật quê quề quế quể quễ vệ
            quên quền quến quển quễn quyện quốc cuộc quyết quyệt quý quỳ quỹ quỷ quỵ ra
            rà rá rả rã rạ rác rạc rạch rai rài rái rải rãi rại ram ràm
            rám rảm rãm rạm ran ràn rán rản rãn rạn rang ràng ráng rảng rãng rạng
            ranh rành ránh rảnh rãnh rạnh rao rào ráo rảo rão rạo ray rày ráy rảy
            rãy rạy rắc rằm rắm rẳm rẵm rặm răn rằn rắn rẳn rẵn rặn răng rằng
            rắng rẳng rẵng rặng rắp rặp rất rặt râu rầu rấu rẩu rẫu rậu rây rầy
            rấy rẩy rẫy rậy râm rầm rấm rẩm rẫm rậm rấn rẩn rẫn rận rấp rật
            rê rề rế rể rễ rệ rêm rềm rếm rểm rễm rệm rên rền rến rển
            rễn rện rênh rềnh rếnh rểnh rễnh rệnh rếp rệp rết rệt rêu rều rếu rểu
            rễu rệu ri rì rí rỉ rĩ rị ria rìa ría rỉa rĩa rịa rích rịch
            riêng riềng riếng riết riệt riêu riều riếu riểu riễu riệu rim rìm rím rỉm rĩm
            rịm rin rìn rín rỉn rĩn rịn rinh rình rính rỉnh rĩnh rịnh ríp rịp rít
            rịt riu rìu ríu rỉu rĩu rịu ro rò ró rỏ rọ roa roà roá roả
            roã rọc rỏi rói rọi rom ròm róm rỏm rõm rọm ron ròn rón rỏn rõn
            rọn rong ròng róng rỏng rõng rọng rô rồ rố rổ rỗ rốc rồi rối rổi
            rỗi rội rôm rồm rốm rổm rỗm rộm rôn rồn rốn rổn rỗn rộn rông rồng
            rống rổng rỗng rộng rốt rột rơ rờ rớ rở rỡ rợ rơi rời rới rởi
            rỡi rợi rơm rờm rớm rởm rỡm rợm rơn rờn rớn rởn rỡn rợn rớp rợp
            rớt rợt ru rù rú rủ rũ rụ rua rùa rúa rủa rũa rụa rúc rục
            rui rùi rúi rủi rũi rụi rum rùm rúm rủm rũm rụm run rùn rún rủn
            rũn rụn rung rùng rúng rủng rũng rụng ruốc ruôi ruồi ruối ruổi ruỗi ruội ruột
            rúp rụp rút rụt rưaừa rứa rửa rữa rựa rức rực rươi rười rưới rưởi rưỡi
            rượi rươm rườm rướm rưởm rưỡm rượm rươn rườn rướn rưởn rưỡn rượn rương rường rướng
            rưởng rưỡng rượng rước rược rướt rượt rứt rựt sa sà sá sả sã sạ xác
            sạc sạch sai sài sái sải sãi sại sam sàm sám sảm sãm sạm san sàn
            sán sản sẵn sạn sang sàng sáng sảng sãng sạng sanh sành sánh sảnh sãnh sạnh
            sao sào sáo sảo são sạo say sày sáy sảy sãy sạy sắc sằm sắm sẳm
            sẵm sặm săn sằn sắn sẳn sặn xăng sằng sắng sẳng sẵng sặng sắp sặp sắt
            sặt sâu sầu xấu sẩu sẫu sậu xây sầy sấy sẩy sẫy sậy xâm sầm sẩm
            sẫm sậm sân sần sấn sẩn sận sật sê sề sế sể sễ sệ sêm sềm
            sếm sểm sễm sệm sên sền sến sển sễn sện sênh sềnh sếnh sểnh sễnh sệnh
            xếp sệp sết sệt sêu sều sếu sểu sễu sệu si sì sí sỉ sĩ sị
            sia sìa sía sỉa sĩa sịa xích sịch siêu siều siếu siểu siễu siệu sim sìm
            sím sỉm sĩm sịm sin sìn sín sỉn sĩn sịn sinh sình sính sỉnh sĩnh sịnh
            síp sịp sít sịt siu sìu síu sỉu sĩu sịu so sò só sỏ sõ sọ
            soa soà soá soả soã soạ soạn soát sọc sỏi sói sọi som sòm sóm sỏm
            sõm sọm son sòn són sỏn sõn sọn song sòng sóng sỏng sõng sọng sô sồ
            số sổ sỗ sộ sốc sộc sôi sổi sỗi sội sồm sốm sổm sỗm sộm sôn
            sồn sốn sổn sỗn sộn sông sồng sống sổng sỗng sộng sốt sột sơ sờ sớ
            sở sỡ sợ sơi sời sới sỡi sợi sớm sờm sởm sỡm sợm sơn sờn sớn
            sởn sỡn sợn sớp sợp sớt sợt su sù sú sủ sũ sụ sua sùa súa
            sủa sũa sụa xúc sục sui sùi súi sủi sũi sụi sum sùm súm sủm sũm
            sụm sun sùn sún sủn sũn sụn sung sùng súng sủng sũng sụng suốt suối suồi
            suổi suỗi suội súp sụp sút sụt suy suỳ suý suỷ suỹ suỵ xuyên xuyền suỷen
            suất luật sư sừ sứ sử sữ sự sưa sừa sứa sửa sữa sựa sức sực
            sươi sười sưới sưởi sưỡi sựơi sươm sườm sưởm sưỡm sựơm sươn sườn sưởn sưỡn sựơn
            sương sường sướng xưởng sưỡng sựơng sước sựơc sướt sựơt sứt sựt ta tà tá tả
            tã tạ tác tạc tách tai tài tái tải tãi tại tam tàm tám tảm tãm
            tạm tan tàn tán tản tãn tạn tang tàng táng tảng tãng tạng tanh tành tánh
            tảnh tãnh tạnh tao tào táo tảo tão tạo tay tày táy tãy tạy tắc tằm
            tắm tẳm tẵm tặm tăn tằn tắn tẳn tẵn tặn tăng tằng tắng tẵng tặng tắp
            tập tắt tặt tâu tàu tấu tẩu tẫu tậu tây tầy tấy tẩy tẫy tậy tâm
            tầm tấm tẩm tân tần tấn tẩn tẫn tận tấp tật tê tề tế tể tễ
            tệ têm tềm tếm tểm tễm tiệm tếnh tển tễn tiện tênh tềnh tểnh tễnh tệnh
            tếp tệp tết tệt têu tều tếu tểu tễu tệu ti tì tí tỉ tĩ tị
            tia tìa tía tỉa tĩa tịa tích tịch tiêm tiềm tiếm tiểm tiễm tiên tiền tiến
            tiển tiễn tiếng tiểng tiễng tiếp tiệp tiết tiệt tiêu tiều tiếu tiểu tiễu tiệu tim
            tìm tím tỉm tĩm tịm tin tín tỉn tĩn tịn tinh tình tính tỉnh tĩnh tịnh
            típ tịp tít tịt tiu tìu tíu tỉu tĩu tịu to tò tó tỏ tõ tọ
            toa toà toá toả toã toạ toạc toàn toán toản toãn toạn toang toàng toáng toảng
            toãng toạng toát tọc tỏi tói tọi tom tòm tóm tỏm tõm tọm ton tòn tón
            tỏn tõn tọn tong tòng tóng tỏng tõng tọng tô tồ tố tổ tỗ tộ tốc
            tộc tôi tồi tối tổi tỗi tội tôm tồm tốm tổm tỗm tộm tôn tồn tốn
            tổn tỗn tông tồng tống tổng tỗng tốt tột tơ tờ tớ tở tỡ tợ tơi
            tời tới tởi tỡi tợi tơm tờm tớm tởm tỡm tợm tơn tờn tớn tởn tỡn
            tợn tớp tợp tớt tợt tu tù tú tủ tũ tụ tua túa tủa tũa tụa
            túc tục tui tùi túi tủi tũi tụi tum tùm túm tủm tũm tụm tun tùn
            tún tủn tũn tụn tung tùng túng tủng tũng tụng tuốc tuồi tuối tuổi tuỗi tuội
            tuôn tuồn tuốn tuổn tuỗn tuộn túp tút tụt tuy tuỳ tuý tuỷ tuỹ tuỵ tuyên
            tuyền tuyến tuỷen tuyễn truyện tuyết tuyệt tư từ tứ tử tữ tự tưa từa tứa
            tửa tữa tựa tức tực tươi tười tưới tưởi tưỡi tượi tươm từơm tướm tưởm tưỡm
            tượm tươn từơn tướn tưởn tưỡn tượn tương tường tướng tưởng tưỡng tượng tước tược tướt
            tượt tha thà thá thả thã thạ thác thạc thách thai thài thái thải thãi thại
            tham thàm thám thảm thãm thạm than thàn thán thản thãn thạn thang thàng tháng thẳng
            thãng thạng thanh thành thánh thảnh thãnh thạnh thao thào tháo thảo thão thạo thay thày
            tháy thảy thãy thạy thắc thặc thăm thằm thắm thẳm thẵm thặm thăn thằn thắn thẳn
            thẵn thặn thăng thằng thắng thẵng thặng thắp thặp thắt thật thâu thầu thấu thẩu thẫu
            thậu thây thầy thấy thẩy thẫy thậy thâm thầm thấm thẩm thẫm thậm thân thần thấn
            thẩn thẫn thận thấp thê thề thế thể thễ thệ thêm thềm thếm thểm thễm thệm
            thếnh thển thễn thiện thênh thềnh thểnh thễnh thệnh thếp thệp thết thiệt thêu thều thếu
            thểu thễu thệu thi thì thí thỉ thĩ thị thia thìa thía thỉa thĩa thịa thích
            thịch thiêm thiềm thiếm thiểm thiễm thiệm thiên thiền thiến thiển thiễn thiêng thiềng thiểng thiễng
            thiếp thiệp thiết thiêu thiều thiếu thiểu thiễu thiệu thim thìm thím thỉm thĩm thịm thin
            thìn thín thỉn thĩn thịn thinh thình thính thỉnh thĩnh thịnh thíp thịp thít thịt thiu
            thìu thíu thỉu thĩu thịu tho thò thó thỏ thõ thọ thoa thoà thoá thoả thoã
            thoạ thoái thoải thoại thoan thoàn thoáng thoản thoãn thoạn thoang thoàng thoảng thoãng thoạng thoát
            thọc thỏi thói thọi thom thòm thóm thỏm thõm thọm thon thòn thón thỏn thõn thọn
            thong thòng thóng thỏng thõng thọng thô thồ thố thổ thỗ thộ thốc thộc thôi thồi
            thối thổi thỗi thội thơm thồm thốm thổm thỗm thộm thôn thốn thổn thông thồng thống
            thổng thỗng thốt thột thơ thờ thớ thở thỡ thợ thơi thời thới thởi thỡi thợi
            thờm thớm thởm thỡm thợm thơn thờn thớn thởn thỡn thợn thớp thợp thớt thợt thu
            thù thú thủ thũ thụ thua thùa thúa thủa thũa thụa thúc thục thui thùi thúi
            thủi thũi thụi thum thùm thúm thủm thũm thụm thun thùn thún thủn thũn thụn thung
            thùng thúng thủng thũng thụng thuốc thuôi thuồi thuối thuổi thuỗi thuội thuôn thuồn thuốn thuổn
            thuỗn thuận thúp thút thụt thuê thuề thuế thuể thuễ thuệ thuyền thuyết thư thừ thứ
            thử thữ thự thưa thừa thứa thửa thữa thức thực thươi thười thưới thưởi thưỡi thượi
            thươm thườm thướm thưởm thưỡm thượm thươn thườn thướn thưởn thưỡn thượn thương thường thướng thưởng
            thưỡng thượng thước thược thướt thượt tra trà trá trả trã trạ trác trạc trách trai
            trài trải trãi trại tram tràm trám trảm trãm trạm tran tràn trán trản trãn trạn
            trang tràng tráng trảng trãng trạng tranh trành tránh trảnh trãnh trạnh trao trào tráo trảo
            trão trạo tray trày tráy trảu trãy trạy trẳm trẵm trặm trẳn trắng trâu trầu trấu
            trẩu trẫu trậu trây trẩy trẫy trậy trăm trầm chấm trẩm trẫn chấp trật trê trề
            trẻ trễ trẻn trẻnh trêu trều trếu trẻu trễu triệu tri trì trí trĩ trịaia trĩa
            trịa triền triển trinh trình tro trỏ trõ trói chọi trong tròng chóng trỏng trõng trọng
            trô trơ tru trù chú chủ trũ trúc trung trùng trúng truy truỳ truý truỷ truỹ
            truỵ trư trương trướng trưởng trưỡng trượng trước trượt va và vá vả vã vạ vác
            vạc vạch vai vài vái vải vãi vại vam vàm vám vảm vãm vạm van vàn
            ván vản vãn vạn vang vàng váng vảng vãng vạng vanh vành vánh vảnh vãnh vạnh
            vao vào váo vảo vão vạo vay vày váy vảy vãy vạy vắc vằm vắm vẳm
            vẵm vặm văn vằn vắn vẳn vẫn vặn văng vằng vắng vẳng vẵng vặng vắp vặp
            vắt vặt vâu vầu vấu vẩu vẫu vậu vây vầy vấy vẩy vẫy vậyâm vầm vấm
            vẩm vân vần vấn vẩn vận vấp vật vê về vi vì ví vỉ vĩ vị
            via vìa vía vỉa vĩa vịa vích vịch viêm viềm viếm viểm viễm việm viên viền
            viến viển viễn viện viêng viềng viếng viểng viễng việc viết việt viêu viều viếu viểu
            viễu việu vim vìm vím vỉm vĩm vịm vin vìn vín vỉn vĩn vịn vinh vình
            vính vỉnh vĩnh vịnh víp vịp vít vịt viu vìu víu vỉu vĩu vịu vo vò
            vó vỏ võ vọ voi vòi vói vỏi dõi vọi vom vòm vóm vỏm võm vọm
            von vòn vón vỏn võn vong vòng vóng võng vọng vô vồ vố vổ vỗ vốc
            vộc vôi vổi vỗi vộiôm vồm vốm vổm vỗm vồn vốn vổn vỗn vông vổng vỗng
            vốt bột vơ vờ vớ vở vỡ vợ vơi vời với vởi vỡi vợiơm vờm vớm
            vởm vỡm vờn vớn vởn vỡn vợp vớt vợt vu vù vú vủ vũ vụ vua
            vùa vúa vủa vũa vụa vúc vực vui vùi vúi vủi vum vùm vúm vủm vũm
            vụm vun vùn vún vủn vũn vụn vung vùng vúng vủng vũng vụng vuốt vuột vúp
            vút vụt vừa vứa vửa vữa vức vươi vưới vưởi vưỡi vượi vươm vườm vướm vưởm
            vưỡm vượm vươn vườn vướn vưởn vưỡn vượn vương vường vướng vưởng vưỡng vượng vước vượt
            vứt xa xà xá xả xã xạ xạc xách xai xài xái xải xãi xại xam
            xàm xám xảm xãm xạm xan xàn xán xản xãn xạn xang xàng xáng xảng xãng
            xạng xanh xành xánh xảnh xãnh xạnh xao xào xáo xảo xão xạo xay xày xáy
            xảy xãy xạy xắc xằm xắm xẳm xẵm xặm xăn xằn xắn xẳn xặn xằng xắng
            xẳng xẵng xặng xắp xặt xâu xầu xẩu xẫu xậu xầy xấy xẩy xẫy xậy xầm
            xấm xẩm xấn xẩn xẫn xận xấp xật xê xề xế xể xễ xệ xi xì
            xí xỉ xĩ xị xia xìa xía xỉa xĩa xịa xịch xiêm xiềm xiếm xiểm xiễm
            xiệm xiên xiền xiến xiển xiễn xiện xiêng xiềng xiếng xiểng xiễng xiệng xiếp xiệp xiết
            xiệt xiêu xiều xiếu xiểu xiễu xiệu xim xìm xím xỉm xĩm xịm xin xìn xín
            xỉn xĩn xịn xinh xình xính xỉnh xĩnh xịnh xíp xịp xít xịt xiu xìu xíu
            xỉu xĩu xịu xo xò xó xỏ xõ xọ xoa xoà xoá xoả xoã xoạ xoài
            xoải xoại xoan xoàn xoán xoản xoãn xoạn xoang xoàng xoáng xoảng xoãng xoạng xoành xoánh
            xoảnh xoãnh xoạnh xoát xọc xỏi xói xọi xom xòm xóm xỏm xõm xọm xon xòn
            xón xỏn xõn xọn xong xòng xóng xỏng xõng xọng xô xồ xố xổ xỗ xộ
            xốc xộc xôi xổi xỗi xộiôm xồm xốm xổm xỗm xồn xốn xổn xông xổng xỗng
            xơ xờ xớ xở xỡ xợ xơi xời xới xởi xỡi sợiơm xờm xởm xỡm xờn
            xởn xỡn xợp xớt xợt xu xù xú xủ xũ xụ xua xùa xúa xủa xũa
            xụa xục xui xùi xúi xủi xũi xụi xum xùm xúm xủm xũm xụm xun xùn
            xún xủn xũn xụn xung xùng xúng xủng xũng xụng xuôi xuồi xuối xuổi xuỗi xuội
            xuân xuần xuấn xuẩn xuẫn xuận xuất xuật xúp xút xụt xuy xuỳ xuý xuỷ xuỹ
            xuỵ xuỷen xư xừ xứ xử xữ xự xưa xừa xửa xữa xựa xức xực xươi
            xười xưới xưởi xưỡi xượi xươm xườm xưởm xưỡm xượm xươn xườn xưởn xưỡn xượn xương
            xường xướng xưỡng xượng xước xược xướt xượt yêu yếu yểu yểm yến yên yết yệt
        """.trimIndent().split(Regex("\\s+"))
        val viSyllableRegex = Regex("^[a-záàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđ]{2,7}$")
        for (w in commonVietnameseWords) {
            val clean = w.trim().lowercase()
            if (clean.length in 2..7 && viSyllableRegex.matches(clean) && !containsKey(clean)) {
                put(clean, 72)
            }
        }

        // Merge pretrained words from vnchflex-trained-data.json (offline corpus pretrain)
        trainedData.words.forEach { w ->
            if (w.length in 2..20 && !containsKey(w)) put(w, 78)
        }
    }

    // Bundled Trigram Language Model: "prev2 prev1" -> ordered list of next words (gộp dữ liệu mặc định + assets/vi_trigrams.json)
    private val bundledTrigrams: Map<String, List<String>> = buildMap {
        val fallbackTrigrams = mapOf(
            "xin chào" to listOf("bạn", "mọi người", "anh", "chị"),
            "cảm ơn" to listOf("bạn", "anh", "chị", "nhiều"),
            "rất vui" to listOf("được", "khi", "vì"),
            "vui được" to listOf("gặp", "làm", "biết"),
            "được gặp" to listOf("bạn", "anh", "mọi người"),
            "hôm nay" to listOf("tôi", "bạn", "trời", "mình"),
            "ngày mai" to listOf("mình", "tôi", "bạn", "đi"),
            "bây giờ" to listOf("mình", "tôi", "bạn", "đang"),
            "thành phố" to listOf("hồ chí minh", "hà nội", "đà nẵng"),
            "hồ chí" to listOf("minh", "minh và"),
            "việt nam" to listOf("ta", "mình", "vô địch", "ơi"),
            "tiếng việt" to listOf("rất", "chuẩn", "tự do"),
            "bỏ dấu" to listOf("tự do", "tiếng việt", "nhanh"),
            "dấu tự" to listOf("do", "động"),
            "bàn phím" to listOf("tiếng việt", "gboard", "android"),
            "số điện" to listOf("thoại", "thoại của"),
            "điện thoại" to listOf("của", "di động", "mới"),
            "không biết" to listOf("có", "sao", "là", "nữa"),
            "tôi đang" to listOf("làm", "ở", "đi", "học"),
            "mình đang" to listOf("ở", "làm", "đi", "ăn"),
            "bạn đang" to listOf("làm gì", "ở đâu", "đi đâu"),
            "đang làm" to listOf("gì", "việc", "bài"),
            "làm gì" to listOf("đó", "vậy", "thế", "dzậy"),
            "đi dzìa" to listOf("nhà", "quê", "sớm", "thôi"),
            "mới dzìa" to listOf("tới", "nhà", "luôn"),
            "bá cháy" to listOf("bọ chét", "luôn", "thiệt"),
            "số dzách" to listOf("luôn", "thiệt", "nha"),
            "hết sảy" to listOf("con bà bảy", "luôn", "nha"),
            "chúc bạn" to listOf("một ngày", "vui vẻ", "thành công"),
            "một ngày" to listOf("tốt lành", "vui vẻ", "mới"),
        )
        putAll(fallbackTrigrams)

        val assetTrigrams = loadAssetNgrams(context, "vi_trigrams.json")
        assetTrigrams.forEach { (ctx, words) ->
            val merged = LinkedHashSet<String>()
            get(ctx)?.let { merged.addAll(it) }
            merged.addAll(words)
            put(ctx, merged.toList())
        }
        trainedData.trigrams.forEach { (ctx, words) ->
            val merged = LinkedHashSet<String>()
            get(ctx)?.let { merged.addAll(it) }
            merged.addAll(words)
            put(ctx, merged.toList())
        }
    }

    // Bundled Bigram Language Model: "prev1" -> ordered list of next words (gộp dữ liệu mặc định + assets/vi_bigrams.json)
    private val bundledBigrams: Map<String, List<String>> = buildMap {
        val fallbackBigrams = mapOf(
            "xin" to listOf("chào", "lỗi", "cảm ơn", "hỏi"),
            "cảm" to listOf("ơn", "thấy", "xúc", "giác"),
            "việt" to listOf("nam", "ngữ", "kiều"),
            "hôm" to listOf("nay", "qua", "trước", "đó"),
            "ngày" to listOf("mai", "nay", "xưa", "mới"),
            "thành" to listOf("phố", "công", "viên", "thật"),
            "bàn" to listOf("phím", "làm", "bạc", "ăn"),
            "không" to listOf("biết", "có", "được", "thể"),
            "tôi" to listOf("đang", "muốn", "là", "có"),
            "mình" to listOf("đang", "sẽ", "cũng", "đi"),
            "bạn" to listOf("có", "đang", "ơi", "nhé"),
            "đang" to listOf("làm", "ở", "đi", "học"),
            "làm" to listOf("việc", "gì", "sao", "ơn"),
            "đi" to listOf("dzìa", "học", "làm", "chơi"),
            "dzìa" to listOf("nhà", "quê", "trễ", "sớm"),
            "dzô" to listOf("đây", "nhà", "một", "luôn"),
            "số" to listOf("dzách", "điện thoại", "lượng", "một"),
            "dzách" to listOf("luôn", "thiệt", "nha"),
            "bá" to listOf("cháy", "đạo", "chủ"),
            "bỏ" to listOf("dấu", "qua", "đi"),
            "dấu" to listOf("tự do", "câu", "thanh"),
            "thời" to listOf("gian", "tiết", "điểm"),
            "công" to listOf("việc", "ty", "nghệ", "nhận"),
            "sài" to listOf("gòn", "thành"),
            "hà" to listOf("nội", "đông"),
            "đà" to listOf("nẵng", "lạt"),
            "rất" to listOf("vui", "nhiều", "tốt", "hay"),
        )
        putAll(fallbackBigrams)

        val assetBigrams = loadAssetNgrams(context, "vi_bigrams.json")
        assetBigrams.forEach { (ctx, words) ->
            val merged = LinkedHashSet<String>()
            get(ctx)?.let { merged.addAll(it) }
            merged.addAll(words)
            put(ctx, merged.toList())
        }
        trainedData.bigrams.forEach { (ctx, words) ->
            val merged = LinkedHashSet<String>()
            get(ctx)?.let { merged.addAll(it) }
            merged.addAll(words)
            put(ctx, merged.toList())
        }
    }

    private val englishContractions = mapOf(
        "dont" to "don't", "cant" to "can't", "wont" to "won't", "didnt" to "didn't",
        "doesnt" to "doesn't", "isnt" to "isn't", "arent" to "aren't", "wasnt" to "wasn't",
        "werent" to "weren't", "havent" to "haven't", "hasnt" to "hasn't", "hadnt" to "hadn't",
        "wouldnt" to "wouldn't", "couldnt" to "couldn't", "shouldnt" to "shouldn't",
        "im" to "I'm", "ive" to "I've", "ill" to "I'll", "id" to "I'd",
        "youre" to "you're", "youve" to "you've", "youll" to "you'll", "youd" to "you'd",
        "hes" to "he's", "shes" to "she's", "its" to "it's", "theyre" to "they're",
        "thats" to "that's", "whats" to "what's", "heres" to "here's", "theres" to "there's",
        "lets" to "let's", "whos" to "who's", "wheres" to "where's", "hows" to "how's"
    )

    private val emojiMap = mapOf(
        "vui" to "😊", "dzui" to "😄", "cười" to "😂", "yêu" to "❤️", "tim" to "❤️",
        "ok" to "👍", "được" to "👍", "dzách" to "🔥", "dzô" to "🍻", "buồn" to "😢",
        "khóc" to "😭", "chào" to "👋", "cafe" to "☕", "lửa" to "🔥",
        "bia" to "🍺", "rượu" to "🍷", "mưa" to "☔", "nắng" to "☀️", "trời" to "🌤️",
        "trà" to "🍵", "cơm" to "🍚", "phở" to "🍜", "bún" to "🍜", "bánh" to "🍰",
        "ngủ" to "😴", "mệt" to "😩", "tiền" to "💰", "hoa" to "🌸", "chó" to "🐶",
        "mèo" to "🐱", "xe" to "🚗", "nhà" to "🏠", "nhạc" to "🎵", "ảnh" to "📷",
        "chúc" to "🎉", "quà" to "🎁", "sách" to "📚", "sao" to "⭐", "nóng" to "🥵",
        "lạnh" to "🥶", "đói" to "😋", "ngon" to "😋", "tức" to "😡", "giận" to "😠",
        "love" to "❤️", "beer" to "🍺", "rain" to "☔", "coffee" to "☕", "happy" to "😊",
        "sad" to "😢", "fire" to "🔥", "star" to "⭐", "music" to "🎵", "sleep" to "😴",
    )

    private val strippedWordCache = HashMap<String, String>(8192)
    private val userUnigramCounts = mutableMapOf<String, Int>()
    private val userBigrams = mutableMapOf<String, MutableMap<String, Int>>()
    private val userTrigrams = mutableMapOf<String, MutableMap<String, Int>>()

    private class TrieNode {
        val children = HashMap<Char, TrieNode>(8)
        val words = ArrayList<String>(2)
    }

    private val strippedTrieRoot = TrieNode()
    private var trieBuilt = false

    private fun ensureTrieBuilt() {
        if (trieBuilt) return
        trieBuilt = true
        coreUnigramWeights.keys.forEach { insertIntoTrie(it) }
        englishUnigramWeights.keys.forEach { insertIntoTrie(it) }
        userUnigramCounts.keys.forEach { insertIntoTrie(it) }
    }

    private fun insertIntoTrie(word: String) {
        val stripped = strippedWordCache.getOrPut(word) { DiacriticUtils.strip(word).lowercase() }
        var node = strippedTrieRoot
        for (c in stripped) {
            node = node.children.getOrPut(c) { TrieNode() }
        }
        if (!node.words.contains(word)) {
            node.words.add(word)
        }
    }

    private fun collectFromTrie(prefixStripped: String, limit: Int = 48): List<String> = synchronized(learnLock) {
        ensureTrieBuilt()
        var node = strippedTrieRoot
        for (c in prefixStripped) {
            node = node.children[c] ?: return@synchronized emptyList()
        }
        val out = ArrayList<String>(limit)
        val queue = ArrayDeque<TrieNode>()
        queue.add(node)
        while (queue.isNotEmpty() && out.size < limit) {
            val cur = queue.removeFirst()
            for (w in cur.words) {
                out.add(w)
                if (out.size >= limit) break
            }
            if (out.size < limit) {
                cur.children.values.forEach { queue.add(it) }
            }
        }
        out
    }

    init {
        dictStore?.loadLearnedWords()?.forEach { w ->
            userUnigramCounts[w] = (userUnigramCounts[w] ?: 0) + 5
        }
        dictStore?.loadLearnedBigrams()?.forEach { (ctx, map) ->
            userBigrams[ctx] = map.toMutableMap()
        }
        dictStore?.loadLearnedTrigrams()?.forEach { (ctx, map) ->
            userTrigrams[ctx] = map.toMutableMap()
        }
    }

    /**
     * Gợi ý từ đang gõ (Word Suggestions):
     * - Khớp tiền tố có dấu và tiền tố không dấu (`"viet"` -> `"việt"`, `"dzia"` -> `"dzìa"`, `"dzach"` -> `"dzách"`).
     * - Ưu tiên các từ phù hợp với ngữ cảnh Trigram `(prevWord2, prevWord1)` và Bigram `(prevWord1)`.
     */
    fun suggest(
        prefix: String,
        prevWord1: String = "",
        prevWord2: String = "",
        limit: Int = 3,
        includeEmoji: Boolean = true,
        macroPreview: String? = null,
    ): List<String> = synchronized(learnLock) {
        val q = prefix.trim()
        if (q.isEmpty()) return@synchronized emptyList()
        val lower = q.lowercase()
        val strippedQuery = DiacriticUtils.strip(lower)
        val isUpperFirst = q.first().isUpperCase()

        val results = mutableListOf<String>()
        // Slot 0 (Gboard left slot): exact composing text
        results.add(q)

        englishContractions[lower]?.let { contraction ->
            if (!results.contains(contraction)) results.add(contraction)
        }

        // Slot 1 priority: Macro preview or Personal Dictionary shortcut
        if (!macroPreview.isNullOrEmpty() && !results.contains(macroPreview)) {
            results.add(macroPreview)
        }

        val personalWords = dictStore?.loadPersonalWords().orEmpty()
        personalWords.forEach { pw ->
            val pwLower = pw.word.lowercase()
            val pwStripped = DiacriticUtils.strip(pwLower)
            if (pw.shortcut.equals(lower, ignoreCase = true) ||
                pwLower.startsWith(lower) ||
                pwStripped.startsWith(strippedQuery)
            ) {
                if (!results.contains(pw.word)) results.add(pw.word)
            }
        }

        if (includeEmoji) {
            (emojiMap[lower] ?: emojiMap[strippedQuery])?.let { emoji ->
                if (!results.contains(emoji)) results.add(emoji)
            }
        }

        // Contextual Trigram & Bigram candidate boosts for prefix completion
        val contextualFollowers = getContextualWeights(prevWord2, prevWord1)

        val allWords = collectFromTrie(strippedQuery, 48)
        val scoredCandidates = allWords
            .asSequence()
            .filter { candidate ->
                if (candidate.equals(lower, ignoreCase = true)) return@filter false
                val candStripped = strippedWordCache.getOrPut(candidate) { DiacriticUtils.strip(candidate) }
                candidate.startsWith(lower) || candStripped.startsWith(strippedQuery)
            }
            .map { candidate ->
                val exactPrefixBoost = if (candidate.startsWith(lower)) 250 else 100
                val contextBoost = (contextualFollowers[candidate] ?: 0) * 120
                val userBoost = (userUnigramCounts[candidate] ?: 0) * 15
                val baseWeight = coreUnigramWeights[candidate] ?: 50
                val lengthPenalty = (candidate.length - lower.length).coerceAtLeast(0) * 4
                val totalScore = exactPrefixBoost + contextBoost + userBoost + baseWeight - lengthPenalty
                candidate to totalScore
            }
            .sortedByDescending { it.second }

        scoredCandidates.forEach { (candidate, _) ->
            val cased = if (isUpperFirst) candidate.replaceFirstChar { it.uppercaseChar() } else candidate
            if (!results.contains(cased)) results.add(cased)
        }

        results.take(limit)
    }

    /**
     * Gợi ý từ tiếp theo theo mô hình Trigram `(prevWord2, prevWord1) -> nextWord`
     * với Katz Backoff về Bigram `(prevWord1) -> nextWord` và Unigram.
     */
    fun suggestNextWord(
        prevWord1: String,
        prevWord2: String = "",
        limit: Int = 3,
    ): List<String> = synchronized(learnLock) {
        val p1 = prevWord1.trim().lowercase()
        val p2 = prevWord2.trim().lowercase()
        if (p1.isEmpty()) return@synchronized listOf("Tôi", "Xin", "Mình").take(limit)

        val candidates = LinkedHashSet<String>()

        // 1. Trigram lookup: (p2, p1) -> nextWord
        if (p2.isNotEmpty()) {
            val trigramKey = "$p2 $p1"
            userTrigrams[trigramKey]?.entries
                ?.sortedByDescending { it.value }
                ?.forEach { candidates.add(it.key) }
            bundledTrigrams[trigramKey]?.forEach { candidates.add(it) }
        }

        // 2. Katz Backoff -> Bigram lookup: (p1) -> nextWord
        if (candidates.size < limit) {
            userBigrams[p1]?.entries
                ?.sortedByDescending { it.value }
                ?.forEach { candidates.add(it.key) }
            bundledBigrams[p1]?.forEach { candidates.add(it) }
        }

        // 3. Backoff -> High-frequency Vietnamese connectors / unigrams
        if (candidates.size < limit) {
            listOf("là", "có", "và", "được", "không", "nha").forEach {
                if (!it.equals(p1, ignoreCase = true)) candidates.add(it)
            }
        }

        candidates.take(limit)
    }

    /**
     * Học từ đơn (Unigram) + cặp từ (Bigram) + bộ 3 từ (Trigram) khi người dùng nhập xong từ.
     */
    fun learn(word: String, prevWord1: String = "", prevWord2: String = "") {
        val w = word.trim().lowercase()
        if (w.length < 2) return
        synchronized(learnLock) {
            // 1. Learn Unigram
            userUnigramCounts[w] = (userUnigramCounts[w] ?: 0) + 1
            dictStore?.addLearnedWord(w)
            insertIntoTrie(w)

            // 2. Learn Bigram (prevWord1 -> w)
            val p1 = prevWord1.trim().lowercase()
            if (p1.isNotEmpty()) {
                val bMap = userBigrams.getOrPut(p1) { mutableMapOf() }
                bMap[w] = (bMap[w] ?: 0) + 1
                dictStore?.saveLearnedBigrams(userBigrams)

                // 3. Learn Trigram (prevWord2 + " " + prevWord1 -> w)
                val p2 = prevWord2.trim().lowercase()
                if (p2.isNotEmpty()) {
                    val tKey = "$p2 $p1"
                    val tMap = userTrigrams.getOrPut(tKey) { mutableMapOf() }
                    tMap[w] = (tMap[w] ?: 0) + 1
                    dictStore?.saveLearnedTrigrams(userTrigrams)
                }
            }
        }
    }

    /**
     * Học nhanh Unigram, Bigram và Trigram từ một đoạn văn bản mẫu (email, ghi chú, file .txt).
     * Tách theo dấu câu để không ghép nhầm từ cuối câu trước với từ đầu câu sau.
     * @return Số lượng từ hợp lệ đã được xử lý và đưa vào mô hình ngôn ngữ.
     */
    fun learnFromCorpus(corpusText: String): Int {
        if (corpusText.isBlank()) return 0
        val sentenceSplitter = Regex("[.!?\\n;:\\(\\)\\[\\]\"“”]+")
        val validTokenRegex = Regex("^[a-záàảãạăắằẳẵặâấầẩẫậéèẻẽẹêếềểễệíìỉĩịóòỏõọôốồổỗộơớờởỡợúùủũụưứừửữựýỳỷỹỵđ0-9'-]{2,20}$")

        var learnedWordCount = 0
        val sentences = sentenceSplitter.split(corpusText.lowercase())

        synchronized(learnLock) {
            for (sentence in sentences) {
                val tokens = sentence.trim()
                    .split(Regex("\\s+"))
                    .map { it.trim('-', '\'') }
                    .filter { validTokenRegex.matches(it) }

                if (tokens.isEmpty()) continue

                var prev1 = ""
                var prev2 = ""
                for (token in tokens) {
                    userUnigramCounts[token] = (userUnigramCounts[token] ?: 0) + 1
                    dictStore?.addLearnedWord(token)
                    insertIntoTrie(token)

                    if (prev1.isNotEmpty()) {
                        val bMap = userBigrams.getOrPut(prev1) { mutableMapOf() }
                        bMap[token] = (bMap[token] ?: 0) + 1

                        if (prev2.isNotEmpty()) {
                            val tKey = "$prev2 $prev1"
                            val tMap = userTrigrams.getOrPut(tKey) { mutableMapOf() }
                            tMap[token] = (tMap[token] ?: 0) + 1
                        }
                    }

                    prev2 = prev1
                    prev1 = token
                    learnedWordCount++
                }
            }

            if (learnedWordCount > 0) {
                dictStore?.saveLearnedBigrams(userBigrams)
                dictStore?.saveLearnedTrigrams(userTrigrams)
                dictStore?.flushNgrams()
            }
        }
        return learnedWordCount
    }

    fun clearLearnedWords() {
        synchronized(learnLock) {
            userUnigramCounts.clear()
            userBigrams.clear()
            userTrigrams.clear()
        }
        dictStore?.clearLearnedWords()
    }

    /**
     * Nạp lại toàn bộ từ đã học, Bigram và Trigram từ PersonalDictionaryStore
     * (dùng sau khi Import backup hoặc Xóa từ đã học trong SettingsActivity).
     */
    fun reloadFromStore() {
        val store = dictStore ?: return
        store.invalidateCaches()
        synchronized(learnLock) {
            userUnigramCounts.clear()
            userBigrams.clear()
            userTrigrams.clear()

            store.loadLearnedWords().forEach { w ->
                userUnigramCounts[w] = (userUnigramCounts[w] ?: 0) + 5
                insertIntoTrie(w)
            }
            store.loadLearnedBigrams().forEach { (ctx, map) ->
                userBigrams[ctx] = map.toMutableMap()
            }
            store.loadLearnedTrigrams().forEach { (ctx, map) ->
                userTrigrams[ctx] = map.toMutableMap()
            }
        }
    }

    private val englishUnigramWeights: Map<String, Int> = buildMap {
        val loadedFromAsset = runCatching {
            context?.assets?.open("en_unigrams.txt")?.bufferedReader()?.useLines { lines ->
                lines.forEach { line ->
                    val parts = line.trim().split('\t')
                    val word = parts.firstOrNull()?.lowercase().orEmpty()
                    val weight = parts.getOrNull(1)?.toIntOrNull() ?: 75
                    if (word.length >= 2 && word.all { it in 'a'..'z' }) {
                        putIfAbsent(word, weight)
                    }
                }
            }
        }.isSuccess && isNotEmpty()

        if (!loadedFromAsset) {
            listOf(
                "the", "and", "you", "love", "hello", "good", "thanks", "ok", "make",
                "that", "this", "with", "have", "from", "they", "what", "when", "where"
            ).forEach { put(it, 95) }
        }
    }

    fun getGlideVocabulary(isVietnameseMode: Boolean = true): Map<String, Int> {
        return LinkedHashMap<String, Int>(coreUnigramWeights.size + englishUnigramWeights.size + userUnigramCounts.size).apply {
            if (isVietnameseMode) {
                englishUnigramWeights.forEach { (w, weight) ->
                    put(w, (weight - 12).coerceIn(55, 84))
                }
                putAll(coreUnigramWeights)
            } else {
                putAll(englishUnigramWeights)
            }
            userUnigramCounts.forEach { (w, count) ->
                put(w, (get(w) ?: 65) + (count * 12).coerceAtMost(80))
            }
        }
    }

    fun isKnownWord(word: String): Boolean {
        val clean = word.trim().lowercase()
        if (clean.isEmpty()) return false
        return coreUnigramWeights.containsKey(clean) ||
            englishUnigramWeights.containsKey(clean) ||
            englishContractions.containsValue(word) ||
            userUnigramCounts.containsKey(clean)
    }

    fun getContextBoosts(prevWord2: String, prevWord1: String): Map<String, Int> =
        getContextualWeights(prevWord2, prevWord1)

    fun flushPendingSaves() {
        dictStore?.flushNgrams()
    }

    private fun getContextualWeights(prevWord2: String, prevWord1: String): Map<String, Int> = synchronized(learnLock) {
        val weights = mutableMapOf<String, Int>()
        val p1 = prevWord1.trim().lowercase()
        val p2 = prevWord2.trim().lowercase()
        if (p1.isEmpty()) return@synchronized weights

        if (p2.isNotEmpty()) {
            val tKey = "$p2 $p1"
            bundledTrigrams[tKey]?.forEachIndexed { idx, w ->
                weights[w] = (weights[w] ?: 0) + (10 - idx).coerceAtLeast(3)
            }
            userTrigrams[tKey]?.forEach { (w, count) ->
                weights[w] = (weights[w] ?: 0) + count * 6
            }
        }

        bundledBigrams[p1]?.forEachIndexed { idx, w ->
            weights[w] = (weights[w] ?: 0) + (5 - idx).coerceAtLeast(1)
        }
        userBigrams[p1]?.forEach { (w, count) ->
            weights[w] = (weights[w] ?: 0) + count * 3
        }
        weights
    }

    private fun loadAssetNgrams(context: Context?, assetName: String): Map<String, List<String>> {
        if (context == null) return emptyMap()
        return runCatching {
            val raw = context.assets.open(assetName).bufferedReader().use { it.readText() }
            val json = org.json.JSONObject(raw)
            buildMap {
                val keys = json.keys()
                while (keys.hasNext()) {
                    val k = keys.next().trim().lowercase()
                    val arr = json.optJSONArray(k) ?: continue
                    val followers = buildList(arr.length()) {
                        for (i in 0 until arr.length()) {
                            val w = arr.optString(i, "").trim().lowercase()
                            if (w.isNotEmpty()) add(w)
                        }
                    }
                    if (k.isNotEmpty() && followers.isNotEmpty()) {
                        put(k, followers)
                    }
                }
            }
        }.getOrDefault(emptyMap())
    }

    private data class TrainedData(
        val words: List<String>,
        val bigrams: Map<String, List<String>>,
        val trigrams: Map<String, List<String>>
    )

    private fun loadTrainedDataAsset(context: Context?): TrainedData {
        if (context == null) return TrainedData(emptyList(), emptyMap(), emptyMap())
        return runCatching {
            val raw = context.assets.open("vnchflex-trained-data.json").bufferedReader().use { it.readText() }
            val root = org.json.JSONObject(raw)
            val dict = root.optJSONObject("dictionary") ?: root

            val wordsJson = dict.optJSONArray("learnedWords")
            val words = buildList(wordsJson?.length() ?: 0) {
                if (wordsJson != null) {
                    for (i in 0 until wordsJson.length()) {
                        val w = wordsJson.optString(i, "").trim().lowercase()
                        if (w.length in 2..20) add(w)
                    }
                }
            }

            fun parseCountNgrams(key: String, limit: Int = 8): Map<String, List<String>> {
                val obj = dict.optJSONObject(key) ?: return emptyMap()
                val out = HashMap<String, List<String>>(obj.length())
                val keys = obj.keys()
                while (keys.hasNext()) {
                    val ctx = keys.next().trim().lowercase()
                    val followersObj = obj.optJSONObject(ctx) ?: continue
                    val scored = ArrayList<Pair<String, Int>>(followersObj.length())
                    val fKeys = followersObj.keys()
                    while (fKeys.hasNext()) {
                        val w = fKeys.next().trim().lowercase()
                        val c = followersObj.optInt(w, 0)
                        if (w.isNotEmpty() && c > 0) scored.add(w to c)
                    }
                    if (ctx.isNotEmpty() && scored.isNotEmpty()) {
                        scored.sortByDescending { it.second }
                        out[ctx] = scored.take(limit).map { it.first }
                    }
                }
                return out
            }

            TrainedData(
                words = words,
                bigrams = parseCountNgrams("learnedBigrams"),
                trigrams = parseCountNgrams("learnedTrigrams")
            )
        }.getOrDefault(TrainedData(emptyList(), emptyMap(), emptyMap()))
    }
}
