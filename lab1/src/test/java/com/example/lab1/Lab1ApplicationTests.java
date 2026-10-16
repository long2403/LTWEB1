package com.example.lab1;

import com.example.lab1.entity.Book;
import com.example.lab1.entity.Chapter;
import com.example.lab1.repository.BookRepository;
import com.example.lab1.repository.ChapterRepository;
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
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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

	@Test
	void contextLoads() {
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

	    mockMvc.perform(post("/api/books/" + book.getId() + "/comments")
	            .session(authenticatedSession)
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"username\":\"spoofed_admin\",\"content\":\"Great story\",\"rating\":5}"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.username").value("reader_test"));

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
	    mockMvc.perform(post("/api/books")
	            .with(user("admin").roles("ADMIN"))
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"title\":\"Missing CSRF token\"}"))
	        .andExpect(status().isForbidden());

	    mockMvc.perform(post("/api/books")
	            .with(user("admin").roles("ADMIN"))
	            .with(csrf())
	            .contentType(MediaType.APPLICATION_JSON)
	            .content("{\"title\":\"Admin Story\",\"author\":\"Admin\",\"genre\":\"Fantasy\"}"))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.title").value("Admin Story"));

	    mockMvc.perform(get("/api/admin/summary").with(user("admin").roles("ADMIN")))
	        .andExpect(status().isOk())
	        .andExpect(jsonPath("$.books").exists())
	        .andExpect(jsonPath("$.chapters").exists())
	        .andExpect(jsonPath("$.comments").exists());
	}
}
