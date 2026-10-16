package com.example.lab1.config;

import com.example.lab1.entity.Book;
import com.example.lab1.entity.Chapter;
import com.example.lab1.repository.BookRepository;
import com.example.lab1.repository.ChapterRepository;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoCatalogInitializer implements ApplicationRunner {
    private final BookRepository books;
    private final ChapterRepository chapters;

    public DemoCatalogInitializer(BookRepository books, ChapterRepository chapters) {
        this.books = books;
        this.chapters = chapters;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (DemoStory story : DEMO_STORIES) {
            Book book = books.findFirstByTitleIgnoreCase(story.title()).orElseGet(() -> createBook(story));
            for (int number = 1; number <= 3; number++) {
                if (!chapters.existsByBookIdAndChapterNumber(book.getId(), number)) {
                    chapters.save(createChapter(book, story, number));
                }
            }
        }
    }

    private Book createBook(DemoStory story) {
        Book book = new Book();
        book.setTitle(story.title());
        book.setAuthor(story.author());
        book.setGenre(story.genre());
        book.setDescription(story.description());
        book.setStatus("Đang ra");
        book.setStoryType(story.storyType());
        book.setRecommended(story.recommended());
        return books.save(book);
    }

    private Chapter createChapter(Book book, DemoStory story, int number) {
        String title = switch (number) {
            case 1 -> "Cánh cửa đầu tiên";
            case 2 -> "Dấu vết giữa màn sương";
            default -> "Lời hứa dưới bình minh";
        };
        String opening = switch (number) {
            case 1 -> "Mọi chuyện bắt đầu ở " + story.setting() + ". ";
            case 2 -> "Khi " + story.lead() + " quay lại " + story.setting() + ", một dấu hiệu lạ xuất hiện. ";
            default -> "Bình minh hôm sau mang đến cho " + story.lead() + " một lựa chọn khó khăn. ";
        };
        String content = opening + story.description() + "\n\n"
            + story.lead() + " không biết cuộc gặp tình cờ ấy sẽ thay đổi mọi thứ. "
            + "Giữa những con người có mục tiêu riêng, mỗi manh mối lại mở ra một câu hỏi mới. "
            + "Bạn đồng hành của nhân vật chính đề nghị cùng tìm lời giải, nhưng hành trình chỉ có thể tiếp tục khi cả hai học cách tin tưởng nhau.\n\n"
            + switch (number) {
                case 1 -> "Ở cuối con đường, một chiếc hộp cũ khẽ phát sáng. Bên trong là tấm bản đồ không có tên người vẽ và dòng chữ: “Hãy chọn điều bạn muốn bảo vệ.” " + story.lead() + " quyết định bước tiếp, dù chưa biết phía trước đang chờ đợi điều gì.";
                case 2 -> "Một ký hiệu trên bản đồ trùng khớp với câu chuyện mà người dân địa phương vẫn truyền tai nhau. " + story.lead() + " nhận ra dấu vết không phải lời cảnh báo, mà là lời mời gọi. Tuy nhiên, có ai đó đã đi trước và để lại một cánh cửa vừa khép lại.";
                default -> "Thay vì giữ bí mật cho riêng mình, " + story.lead() + " chia sẻ sự thật với những người đã đồng hành. Họ cùng tìm được lối ra và hiểu rằng điều quý giá nhất không nằm trong chiếc hộp, mà ở lời hứa sẽ tiếp tục sát cánh. Câu chuyện vẫn còn một trang chưa được viết.";
            };
        Chapter chapter = new Chapter();
        chapter.setBookId(book.getId());
        chapter.setChapterNumber(number);
        chapter.setTitle(title);
        chapter.setContent(content);
        return chapter;
    }

    private record DemoStory(
        String title,
        String author,
        String genre,
        String description,
        String storyType,
        boolean recommended,
        String lead,
        String setting
    ) {
    }

    private static final List<DemoStory> DEMO_STORIES = List.of(
        new DemoStory("Mộc Miên Và Thành Phố Trên Mây", "An Nhiên", "Phiêu lưu", "Một người đưa thư trẻ lần theo những lá thư thất lạc giữa các tầng mây để tìm lại con đường về nhà.", "MANGA", true, "Mộc Miên", "ga tàu lơ lửng"),
        new DemoStory("Tiệm Sách Cuối Con Hẻm", "Lam Phương", "Đời thường", "Một tiệm sách chỉ mở cửa vào ngày mưa giúp những vị khách xa lạ tìm thấy câu chuyện họ cần.", "NOVEL", true, "Hạ Vy", "con hẻm phủ đầy hoa giấy"),
        new DemoStory("Bản Đồ Của Người Giữ Rừng", "Minh Khuê", "Kỳ ảo", "Cậu kiểm lâm tập sự phát hiện khu rừng đang tự vẽ lại bản đồ để bảo vệ một bí mật cổ xưa.", "MANGA", true, "Lâm", "trạm gác trong rừng"),
        new DemoStory("Bếp Nhỏ Ở Ngã Tư Mùa Hạ", "Bảo Trân", "Ẩm thực", "Những món ăn bình dị kết nối các thành viên trong khu phố qua một mùa hè nhiều thay đổi.", "NOVEL", false, "Thu An", "quán ăn nhỏ ở ngã tư"),
        new DemoStory("Đội Sao Băng Số Bảy", "Hải Đăng", "Khoa học viễn tưởng", "Nhóm học sinh chế tạo tàu thám hiểm để tìm nguồn năng lượng sạch cho thị trấn ven biển.", "MANGA", true, "Khôi", "xưởng sửa chữa bên bờ biển"),
        new DemoStory("Nhật Ký Của Người Nhặt Nắng", "Diệp Chi", "Chữa lành", "Một cô gái ghi lại những điều tử tế mỗi ngày và dần hàn gắn tình bạn đã bỏ quên.", "NOVEL", false, "Yên", "vườn hoa sau thư viện"),
        new DemoStory("Học Viện Pháp Sư Tập Sự", "Quang Vũ", "Kỳ ảo", "Tân sinh viên không có phép thuật mạnh nhất lại là người duy nhất nghe được tiếng nói của thư viện.", "MANGA", true, "Tú", "tháp chuông của học viện"),
        new DemoStory("Chuyến Xe Buýt Qua Mùa Đông", "Hồng Nhung", "Đời thường", "Tài xế chuyến xe đêm giúp hành khách tìm lại những ước mơ họ tưởng đã bỏ lỡ.", "NOVEL", false, "Bình", "bến xe cuối thành phố"),
        new DemoStory("Hải Đăng Không Tắt", "Thành Nam", "Bí ẩn", "Hai chị em trở về đảo nhỏ và lần theo tín hiệu bí ẩn phát ra từ ngọn hải đăng đã đóng cửa.", "MANGA", false, "Mai", "đường mòn dưới chân hải đăng"),
        new DemoStory("Vườn Trăng Của Bà", "Tường Vi", "Kỳ ảo", "Khu vườn cũ chỉ nở hoa dưới ánh trăng lưu giữ ký ức của nhiều thế hệ trong gia đình.", "NOVEL", true, "Ngọc", "khu vườn sau căn nhà gỗ"),
        new DemoStory("Câu Lạc Bộ Thám Tử Tí Hon", "Gia Hân", "Trinh thám", "Ba người bạn dùng sự tò mò và tinh thần đồng đội để giải những bí ẩn nhỏ quanh trường.", "MANGA", false, "Nam", "phòng dụng cụ thể thao"),
        new DemoStory("Người Gác Đồng Hồ Cát", "Đức Anh", "Kỳ ảo", "Một học việc sửa đồng hồ phát hiện có thể nghe thấy những lựa chọn mà người khác chưa kịp đưa ra.", "NOVEL", true, "Thiện", "tiệm đồng hồ ở quảng trường"),
        new DemoStory("Đường Đua Mùa Gió", "Uyên Linh", "Thể thao", "Đội đua xe đạp của trường tập luyện cho giải đấu và học cách chiến thắng nỗi sợ của mình.", "MANGA", false, "Phúc", "con dốc dẫn ra cánh đồng"),
        new DemoStory("Lời Ca Trên Sân Thượng", "Khánh An", "Âm nhạc", "Một ban nhạc bạn bè tìm lại cảm hứng sáng tác khi thành phố chuẩn bị bước vào lễ hội.", "NOVEL", false, "Vy", "sân thượng nhìn ra bến cảng"),
        new DemoStory("Thợ Máy Của Vương Quốc Gió", "Hoàng Long", "Phiêu lưu", "Cô thợ máy trẻ chế tạo đôi cánh giúp ngôi làng trên núi nối lại liên lạc với thế giới.", "MANGA", true, "Nhi", "xưởng máy trên sườn núi"),
        new DemoStory("Bưu Thiếp Từ Ngày Mai", "Mỹ Duyên", "Khoa học viễn tưởng", "Những tấm bưu thiếp không đề tên người gửi nhắc một chàng trai thay đổi các quyết định nhỏ của hiện tại.", "NOVEL", false, "Đăng", "quầy thư cũ trong nhà ga"),
        new DemoStory("Đảo San Hô Biết Hát", "Phương Thảo", "Phiêu lưu", "Nhóm bạn trẻ bảo vệ rạn san hô bằng cách giải mã nhịp điệu kỳ lạ dưới lòng biển.", "MANGA", false, "San", "bến thuyền của đảo"),
        new DemoStory("Quán Trà Của Những Vì Sao", "Thiên Lam", "Chữa lành", "Một quán trà trên đồi trở thành nơi những vị khách kể lại điều họ muốn bắt đầu lần nữa.", "NOVEL", true, "Trà My", "con đường lát đá lên đồi"),
        new DemoStory("Thư Viện Lúc Nửa Đêm", "Nhật Minh", "Bí ẩn", "Người thủ thư ca đêm tìm kiếm cuốn sách tự thay đổi nội dung theo người đọc.", "MANGA", true, "Duy", "phòng lưu trữ dưới thư viện"),
        new DemoStory("Mùa Hè Trồng Sao", "Mai Anh", "Đời thường", "Một lớp học cùng chăm sóc khu vườn trường và tạo nên dự án khoa học bất ngờ.", "NOVEL", false, "Linh", "nhà kính cũ trong sân trường"),
        new DemoStory("Thành Phố Dưới Lớp Sương", "Trọng Nhân", "Bí ẩn", "Một người vẽ bản đồ tìm thấy những con phố chỉ hiện ra khi sương phủ khắp thung lũng.", "MANGA", false, "Tùng", "đài quan sát trên đỉnh đồi"),
        new DemoStory("Bốn Mùa Ở Nhà Ga Nhỏ", "Ánh Nguyệt", "Đời thường", "Những cuộc gặp thoáng qua tại nhà ga giúp một nhân viên mới hiểu hơn về quê hương mình.", "NOVEL", true, "Hân", "phòng chờ cạnh đường ray"),
        new DemoStory("Phi Hành Đoàn Mầm Xanh", "Bách Khoa", "Khoa học viễn tưởng", "Đội thám hiểm tìm một hành tinh phù hợp để gieo những khu rừng đầu tiên của nhân loại.", "MANGA", false, "Sơn", "nhà kính trên tàu vũ trụ"),
        new DemoStory("Bản Nhạc Trong Chiếc Hộp Gỗ", "Thùy Dương", "Âm nhạc", "Một nhạc công trẻ ghép lại giai điệu dang dở để hiểu lời nhắn của người thầy cũ.", "NOVEL", false, "Di", "phòng tập cạnh nhà hát"),
        new DemoStory("Những Người Bạn Của Gác Chuông", "Anh Thư", "Phiêu lưu", "Nhóm bạn chăm sóc chiếc chuông cổ và khám phá những mật thư được gửi từ nhiều năm trước.", "MANGA", true, "Bảo", "gác chuông giữa quảng trường"),
        new DemoStory("Cửa Tiệm Vá Ước Mơ", "Phúc Nguyên", "Chữa lành", "Một thợ may giúp khách sửa lại những món đồ gắn với ước mơ họ từng cất đi.", "NOVEL", true, "Mẫn", "tiệm may cạnh khu chợ"),
        new DemoStory("Cuộc Thi Robot Tí Hon", "Đông Quân", "Khoa học viễn tưởng", "Hai đội học sinh hợp tác trước vòng chung kết khi phát hiện robot của mình cùng chia sẻ một lỗi bí ẩn.", "MANGA", false, "Khang", "phòng thực hành công nghệ"),
        new DemoStory("Con Đường Qua Cánh Đồng Muối", "Hạ Vy", "Phiêu lưu", "Một cô bé mang chiếc la bàn cũ tìm đường đưa những hạt giống quý đến vùng đất khô hạn.", "NOVEL", false, "Thảo", "chòi canh giữa cánh đồng muối"),
        new DemoStory("Vũ Hội Mặt Nạ Giấy", "Trúc Mai", "Kỳ ảo", "Người làm mặt nạ phát hiện mỗi chiếc mặt nạ đều giữ một câu chuyện chưa được kể.", "MANGA", false, "Mi", "xưởng thủ công ven sông"),
        new DemoStory("Ngày Hội Đèn Lồng Xanh", "Vĩnh Phúc", "Đời thường", "Cả khu phố cùng chuẩn bị lễ hội và tìm cách đưa người hàng xóm ít nói trở lại với cộng đồng.", "NOVEL", true, "Thư", "con phố nhỏ gần bờ sông")
    );
}
