package com.example.lab1;

import com.example.lab1.config.DemoCatalogInitializer;
import com.example.lab1.entity.Book;
import com.example.lab1.entity.Chapter;
import com.example.lab1.repository.BookRepository;
import com.example.lab1.repository.ChapterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class Lab1ApplicationTests {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private BookRepository books;

	@Autowired
	private ChapterRepository chapters;

	@Autowired
	private DemoCatalogInitializer demoCatalogInitializer;

	@Test
	void contextLoads() {
	}

	@Test
	void demoCatalogProvidesEnoughBooksAndChaptersForPagination() throws Exception {
	    org.junit.jupiter.api.Assertions.assertTrue(books.count() >= 30);
	    org.junit.jupiter.api.Assertions.assertTrue(chapters.count() >= 90);
	    Book seededStory = books.findFirstByTitleIgnoreCase("Mộc Miên Và Thành Phố Trên Mây").orElseThrow();
	    org.junit.jupiter.api.Assertions.assertEquals(3, chapters.countByBookId(seededStory.getId()));

	    mockMvc.perform(get("/api/books/page").param("page", "0").param("size", "12"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.content", hasSize(12)))
	        .andExpect(jsonPath("$.totalElements").value(org.hamcrest.Matchers.greaterThanOrEqualTo(30)))
	        .andExpect(jsonPath("$.totalPages").value(org.hamcrest.Matchers.greaterThanOrEqualTo(3)));

	    mockMvc.perform(get("/api/books/page")
	            .param("page", "0")
	            .param("size", "12")
	            .param("search", "Tiệm Sách")
	            .param("storyType", "NOVEL"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.content", hasSize(1)))
	        .andExpect(jsonPath("$.content[0].storyType").value("NOVEL"));

	    Page<Book> first = books.findAll(PageRequest.of(0, 12, Sort.by("id")));
	    Page<Book> second = books.findAll(PageRequest.of(1, 12, Sort.by("id")));
	    java.util.Set<Integer> firstIds = first.getContent().stream().map(Book::getId).collect(java.util.stream.Collectors.toSet());
	    org.junit.jupiter.api.Assertions.assertTrue(second.getContent().stream().noneMatch(book -> firstIds.contains(book.getId())));

	    long bookCount = books.count();
	    long chapterCount = chapters.count();
	    demoCatalogInitializer.run(new org.springframework.boot.DefaultApplicationArguments(new String[0]));
	    org.junit.jupiter.api.Assertions.assertEquals(bookCount, books.count());
	    org.junit.jupiter.api.Assertions.assertEquals(chapterCount, chapters.count());
	}

	@Test
	void publicCatalogDoesNotAllowAnonymousContentChanges() throws Exception {
	    mockMvc.perform(get("/api/books"))
	        .andExpect(status().isOk());

	    mockMvc.perform(get("/add"))
	        .andExpect(status().isForbidden());

	    mockMvc.perform(get("/delete/1"))
	        .andExpect(status().isForbidden());

	    mockMvc.perform(post("/api/books")
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"title\":\"Unauthorized book\"}"))
	        .andExpect(status().isForbidden());
	}

	@Test
	void registrationLoginAndReaderLibraryUseServerSideIdentity() throws Exception {
	    mockMvc.perform(post("/api/auth/register")
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("""
	                {"username":"weak_reader","email":"weak@example.test","password":"weak"}
	                """))
	        .andExpect(status().isBadRequest());

	    mockMvc.perform(post("/api/auth/register")
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("""
	                {"username":"reader_test","email":"reader@example.test","password":"MocReader!Secure2026"}
	                """))
	        .andExpect(status().isCreated())
	        .andExpect(jsonPath("$.username").value("reader_test"));

	    mockMvc.perform(post("/api/auth/register")
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("""
	                {"username":"reader_test","email":"another@example.test","password":"MocReader!Secure2026"}
	                """))
	        .andExpect(status().isConflict());

	    MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("""
	                {"username":"READER_TEST","password":"MocReader!Secure2026"}
	                """))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.role").value("USER"))
	        .andReturn();

	    HttpSession session = loginResult.getRequest().getSession(false);
	    org.junit.jupiter.api.Assertions.assertNotNull(session);
	    MockHttpSession authenticatedSession = (MockHttpSession) session;

	    mockMvc.perform(get("/api/auth/me").session(authenticatedSession))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.authenticated").value(true))
	        .andExpect(jsonPath("$.username").value("reader_test"));
	    mockMvc.perform(get("/").session(authenticatedSession))
	        .andExpect(status().is3xxRedirection())
	        .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl("/index.html"));
	    mockMvc.perform(get("/index.html").session(authenticatedSession))
	        .andExpect(status().isOk());
	    mockMvc.perform(get("/api/auth/me").session(authenticatedSession))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.authenticated").value(true));

	    Book book = new Book();
	    book.setTitle("Test Story");
	    book = books.save(book);
	    Chapter chapter = new Chapter();
	    chapter.setBookId(book.getId());
	    chapter.setChapterNumber(1);
	    chapter.setTitle("First chapter");
	    chapter.setContent("A safe test chapter.");
	    chapters.save(chapter);

	    mockMvc.perform(post("/api/me/favorites")
	            .session(authenticatedSession)
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"bookId\":" + book.getId() + "}"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$", contains(book.getId())));

	    mockMvc.perform(post("/api/me/history")
	            .session(authenticatedSession)
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"bookId\":" + book.getId() + ",\"chapterNumber\":1}"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.chapterNumber").value(1));

	    MvcResult commentResult = mockMvc.perform(post("/api/books/" + book.getId() + "/comments")
	            .session(authenticatedSession)
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"username\":\"spoofed_admin\",\"content\":\"Great story\",\"rating\":5}"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.username").value("reader_test"))
	        .andReturn();
	    Integer commentId = com.jayway.jsonpath.JsonPath.read(
	        commentResult.getResponse().getContentAsString(), "$.id"
	    );

	    MvcResult replyResult = mockMvc.perform(post("/api/books/" + book.getId() + "/comments")
	            .with(user("other_reader").roles("USER"))
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"content\":\"I agree!\",\"parentCommentId\":" + commentId + "}"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.parentCommentId").value(commentId))
	        .andReturn();
	    Integer replyId = com.jayway.jsonpath.JsonPath.read(
	        replyResult.getResponse().getContentAsString(), "$.id"
	    );

	    mockMvc.perform(get("/api/me/notifications").session(authenticatedSession))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$[?(@.type == 'COMMENT_REPLY')]", hasSize(1)));

	    mockMvc.perform(get("/api/me/notifications/unread-count").session(authenticatedSession))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.count").value(1));

	    mockMvc.perform(put("/api/books/" + book.getId() + "/comments/" + replyId)
	            .with(user("other_reader").roles("USER"))
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"content\":\"Edited reply\",\"rating\":null}"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.content").value("Edited reply"));

	    mockMvc.perform(delete("/api/books/" + book.getId() + "/comments/" + replyId)
	            .with(user("third_reader").roles("USER"))
	            .with(csrf()))
	        .andExpect(status().isForbidden());

	    mockMvc.perform(put("/api/books/" + book.getId() + "/comments/" + commentId)
	            .session(authenticatedSession)
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"content\":\"Edited story review\",\"rating\":4}"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.content").value("Edited story review"))
	        .andExpect(jsonPath("$.rating").value(4));

	    mockMvc.perform(put("/api/books/" + book.getId() + "/comments/" + commentId)
	            .with(user("other_reader").roles("USER"))
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"content\":\"Not the author\",\"rating\":1}"))
	        .andExpect(status().isForbidden());

	    mockMvc.perform(post("/api/chapters")
	            .with(user("admin").roles("ADMIN"))
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"bookId\":" + book.getId()
	                + ",\"chapterNumber\":2,\"title\":\"Second chapter\",\"content\":\"More story.\"}"))
	        .andExpect(status().isOk());

	    mockMvc.perform(get("/api/me/notifications").session(authenticatedSession))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$[?(@.type == 'NEW_CHAPTER')]", hasSize(1)));

	    mockMvc.perform(post("/api/books")
	            .with(user("admin").roles("ADMIN"))
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"title\":\"Fresh story\",\"storyType\":\"NOVEL\"}"))
	        .andExpect(status().isOk());

	    mockMvc.perform(get("/api/me/notifications").session(authenticatedSession))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$[?(@.type == 'NEW_BOOK')]", hasSize(1)));

	    mockMvc.perform(post("/api/me/notifications/read-all")
	            .session(authenticatedSession)
	            .with(csrf()))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.updated").value(3));

	    mockMvc.perform(delete("/api/books/" + book.getId() + "/comments/" + commentId)
	            .session(authenticatedSession)
	            .with(csrf()))
	        .andExpect(status().isNoContent());
	    mockMvc.perform(delete("/api/books/" + book.getId() + "/comments/" + replyId)
	            .with(user("other_reader").roles("USER"))
	            .with(csrf()))
	        .andExpect(status().isNoContent());
	    mockMvc.perform(get("/api/books/" + book.getId() + "/comments"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$[?(@.id == " + commentId + ")].deleted", contains(true)))
	        .andExpect(jsonPath("$[?(@.id == " + replyId + ")].deleted", contains(true)));

	    mockMvc.perform(post("/api/books")
	            .session(authenticatedSession)
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"title\":\"Unauthorized book\"}"))
	        .andExpect(status().isForbidden());

	    mockMvc.perform(get("/api/admin/summary").session(authenticatedSession))
	        .andExpect(status().isForbidden());

	    mockMvc.perform(get("/api/students").session(authenticatedSession))
	        .andExpect(status().isForbidden());

	    mockMvc.perform(post("/api/auth/logout")
	            .session(authenticatedSession)
	            .with(csrf()))
	        .andExpect(status().isNoContent());

	    mockMvc.perform(get("/api/auth/me"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.authenticated").value(false));
	}

	@Test
	void adminRoleCanManageContentAndReadTheDashboard() throws Exception {
	    String longCoverUrl = "https://images.example.test/" + "a".repeat(400);
	    mockMvc.perform(post("/api/books")
	            .with(user("admin").roles("ADMIN"))
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"title\":\"Missing CSRF token\"}"))
	        .andExpect(status().isForbidden());

	    MvcResult savedBook = mockMvc.perform(post("/api/books")
	            .with(user("admin").roles("ADMIN"))
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"title\":\"Admin Story\",\"author\":\"Admin\",\"genre\":\"Fantasy\",\"storyType\":\"NOVEL\",\"recommended\":true,\"coverUrl\":\""
	                + longCoverUrl + "\"}"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.title").value("Admin Story"))
	        .andExpect(jsonPath("$.coverUrl").value(longCoverUrl))
	        .andExpect(jsonPath("$.recommended").value(true))
	        .andReturn();

	    Integer id = com.jayway.jsonpath.JsonPath.read(
	        savedBook.getResponse().getContentAsString(),
	        "$.id"
	    );

	    mockMvc.perform(post("/api/books/" + id + "/view").with(csrf()))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.views").value(1));

	    mockMvc.perform(get("/api/books/type/NOVEL"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$[0].storyType").value("NOVEL"));
	    mockMvc.perform(get("/api/books/recommended"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$[0].recommended").value(true));

	    MvcResult comment = mockMvc.perform(post("/api/books/" + id + "/comments")
	            .with(user("moderated_reader").roles("USER"))
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"content\":\"Please moderate this\",\"rating\":4}"))
	        .andExpect(status().isOk())
	        .andReturn();
	    Integer commentId = com.jayway.jsonpath.JsonPath.read(comment.getResponse().getContentAsString(), "$.id");
	    mockMvc.perform(delete("/api/admin/comments/" + commentId)
	            .with(user("admin").roles("ADMIN"))
	            .with(csrf()))
	        .andExpect(status().isOk());
	    mockMvc.perform(get("/api/books/" + id + "/comments"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$[?(@.id == " + commentId + ")].deleted", contains(true)));

	    mockMvc.perform(get("/api/admin/summary").with(user("admin").roles("ADMIN")))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.books").exists())
	        .andExpect(jsonPath("$.chapters").exists())
	        .andExpect(jsonPath("$.comments").exists());
	}

	@Test
	void chatIsPublicToReadButRequiresAnAccountToSendTextOrImages() throws Exception {
	    mockMvc.perform(get("/api/chat/messages"))
	        .andExpect(status().isOk());

	    mockMvc.perform(post("/api/chat/messages")
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"message\":\"anonymous\"}"))
	        .andExpect(status().isForbidden());

	    mockMvc.perform(post("/api/chat/messages")
	            .with(user("chat_reader").roles("USER"))
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"message\":\"Hello, readers!\",\"imageData\":null}"))
	        .andExpect(status().isCreated())
	        .andExpect(jsonPath("$.username").value("chat_reader"))
	        .andExpect(jsonPath("$.message").value("Hello, readers!"));

	    String image = java.util.Base64.getEncoder().encodeToString(new byte[] {1, 2, 3});
	    mockMvc.perform(post("/api/chat/messages")
	            .with(user("chat_reader").roles("USER"))
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"message\":\"\",\"imageData\":\"data:image/png;base64," + image + "\"}"))
	        .andExpect(status().isCreated())
	        .andExpect(jsonPath("$.imageData").value("data:image/png;base64," + image));

	    String oversizedImage = java.util.Base64.getEncoder().encodeToString(new byte[512 * 1024 + 1]);
	    mockMvc.perform(post("/api/chat/messages")
	            .with(user("chat_reader").roles("USER"))
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"message\":\"\",\"imageData\":\"data:image/png;base64," + oversizedImage + "\"}"))
	        .andExpect(status().isPayloadTooLarge());
	}
}
