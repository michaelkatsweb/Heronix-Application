package com.heronix.ui.controller;

import com.heronix.service.HelpService;
import com.heronix.service.HelpService.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Help Center Controller - Manages the in-app help system UI
 * Location: src/main/java/com/heronix/ui/controller/HelpCenterController.java
 *
 * @author Heronix Educational Systems LLC
 * @version 1.0.0
 * @since 2026-01
 */
@Slf4j
@Controller
public class HelpCenterController {

    @Autowired
    private HelpService helpService;

    // Navigation
    @FXML private Button btnGuides;
    @FXML private Button btnGlossary;
    @FXML private Button btnShortcuts;
    @FXML private Label navLabel;
    @FXML private VBox categoryList;

    // Search
    @FXML private TextField searchField;

    // Breadcrumb
    @FXML private Label breadcrumb1;
    @FXML private Label breadcrumb2;
    @FXML private Label breadcrumb3;
    @FXML private Label breadcrumbSep1;
    @FXML private Label breadcrumbSep2;

    // Content views
    @FXML private StackPane contentStack;
    @FXML private VBox guidesView;
    @FXML private VBox glossaryView;
    @FXML private VBox shortcutsView;
    @FXML private VBox searchResultsView;
    @FXML private VBox articleDetailView;

    // Guides view
    @FXML private HBox categoryHeader;
    @FXML private Label categoryIcon;
    @FXML private Label categoryTitle;
    @FXML private Label categoryDesc;
    @FXML private VBox articleContainer;

    // Glossary view
    @FXML private VBox glossaryContainer;

    // Shortcuts view
    @FXML private VBox shortcutsContainer;

    // Search results view
    @FXML private Label searchResultsTitle;
    @FXML private Label searchResultsCount;
    @FXML private VBox searchResultsContainer;

    // Article detail view
    @FXML private VBox articleDetailContainer;

    // State
    private String currentView = "guides";
    private String currentCategoryId = null;
    private String currentArticleId = null;
    private final Deque<String> navigationStack = new ArrayDeque<>();

    @FXML
    public void initialize() {
        log.info("Initializing Help Center Controller");

        // Load initial view (categories)
        handleShowGuides();
    }

    // ========================================================================
    // NAVIGATION HANDLERS
    // ========================================================================

    @FXML
    private void handleShowGuides() {
        log.debug("Showing guides view");
        currentView = "guides";

        // Update nav buttons
        setActiveNavButton(btnGuides);
        navLabel.setText("Categories");

        // Load categories in sidebar
        loadCategoriesInSidebar();

        // Show category overview
        showCategoryOverview();

        // Update breadcrumb
        setBreadcrumb("Help Center", null, null);

        // Show guides view
        showView(guidesView);
    }

    @FXML
    private void handleShowGlossary() {
        log.debug("Showing glossary view");
        currentView = "glossary";

        // Update nav buttons
        setActiveNavButton(btnGlossary);
        navLabel.setText("A-Z Index");

        // Load alphabet in sidebar
        loadAlphabetInSidebar();

        // Load all glossary terms
        loadGlossaryTerms(null);

        // Update breadcrumb
        setBreadcrumb("Help Center", "Glossary", null);

        // Show glossary view
        showView(glossaryView);
    }

    @FXML
    private void handleShowShortcuts() {
        log.debug("Showing shortcuts view");
        currentView = "shortcuts";

        // Update nav buttons
        setActiveNavButton(btnShortcuts);
        navLabel.setText("Categories");

        // Load shortcut categories in sidebar
        loadShortcutCategoriesInSidebar();

        // Load all shortcuts
        loadAllShortcuts();

        // Update breadcrumb
        setBreadcrumb("Help Center", "Keyboard Shortcuts", null);

        // Show shortcuts view
        showView(shortcutsView);
    }

    @FXML
    private void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            handleShowGuides();
            return;
        }

        log.debug("Searching for: {}", query);

        SearchResults results = helpService.search(query);
        showSearchResults(results);
    }

    @FXML
    private void handleBack() {
        if (!navigationStack.isEmpty()) {
            String previous = navigationStack.pop();
            if (previous.startsWith("category:")) {
                String categoryId = previous.substring(9);
                showCategoryArticles(categoryId);
            } else {
                showCategoryOverview();
            }
        } else {
            showCategoryOverview();
        }
    }

    @FXML
    private void handleContactSupport() {
        log.info("Contact support clicked");
        // Could open email client or support form
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Contact Support");
        alert.setHeaderText("Need Help?");
        alert.setContentText("Email: support@heronix.edu\nPhone: 1-800-HERONIX\n\nOr visit our support portal at:\nhttps://support.heronix.edu");
        alert.showAndWait();
    }

    @FXML
    private void handleOnlineDocs() {
        log.info("Online docs clicked");
        // Could open browser to documentation
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Online Documentation");
        alert.setHeaderText("Online Resources");
        alert.setContentText("Full documentation available at:\nhttps://docs.heronix.edu\n\nVideo tutorials:\nhttps://learn.heronix.edu");
        alert.showAndWait();
    }

    // ========================================================================
    // CATEGORY NAVIGATION
    // ========================================================================

    private void loadCategoriesInSidebar() {
        categoryList.getChildren().clear();

        List<HelpCategory> categories = helpService.getAllCategories();
        for (HelpCategory category : categories) {
            Button btn = createCategoryButton(category);
            categoryList.getChildren().add(btn);
        }
    }

    private Button createCategoryButton(HelpCategory category) {
        Button btn = new Button(category.getIcon() + "  " + category.getName());
        btn.getStyleClass().add("help-category-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);

        btn.setOnAction(e -> {
            currentCategoryId = category.getId();
            showCategoryArticles(category.getId());
        });

        return btn;
    }

    private void showCategoryOverview() {
        currentCategoryId = null;
        currentArticleId = null;

        // Hide category header
        categoryHeader.setVisible(false);
        categoryHeader.setManaged(false);

        // Show all categories as cards
        articleContainer.getChildren().clear();

        Label welcomeLabel = new Label("Welcome to Help Center");
        welcomeLabel.getStyleClass().add("help-welcome-title");

        Label descLabel = new Label("Select a category to browse help articles, or use the search box to find specific topics.");
        descLabel.getStyleClass().add("help-welcome-desc");
        descLabel.setWrapText(true);

        articleContainer.getChildren().addAll(welcomeLabel, descLabel);

        // Create category cards grid
        FlowPane categoryGrid = new FlowPane();
        categoryGrid.setHgap(16);
        categoryGrid.setVgap(16);
        categoryGrid.setPadding(new Insets(24, 0, 0, 0));

        List<HelpCategory> categories = helpService.getAllCategories();
        for (HelpCategory category : categories) {
            VBox card = createCategoryCard(category);
            categoryGrid.getChildren().add(card);
        }

        articleContainer.getChildren().add(categoryGrid);

        showView(guidesView);
    }

    private VBox createCategoryCard(HelpCategory category) {
        VBox card = new VBox(8);
        card.getStyleClass().add("help-category-card");
        card.setPadding(new Insets(16));
        card.setPrefWidth(200);
        card.setMinHeight(120);

        Label iconLabel = new Label(category.getIcon());
        iconLabel.getStyleClass().add("help-card-icon");

        Label nameLabel = new Label(category.getName());
        nameLabel.getStyleClass().add("help-card-name");

        Label descLabel = new Label(category.getDescription());
        descLabel.getStyleClass().add("help-card-desc");
        descLabel.setWrapText(true);

        // Article count
        int articleCount = helpService.getArticlesByCategory(category.getId()).size();
        Label countLabel = new Label(articleCount + " articles");
        countLabel.getStyleClass().add("help-card-count");

        card.getChildren().addAll(iconLabel, nameLabel, descLabel, countLabel);

        card.setOnMouseClicked(e -> {
            currentCategoryId = category.getId();
            showCategoryArticles(category.getId());
        });

        return card;
    }

    private void showCategoryArticles(String categoryId) {
        navigationStack.push(currentCategoryId != null ? "category:" + currentCategoryId : "overview");

        currentCategoryId = categoryId;
        currentArticleId = null;

        // Find category
        Optional<HelpCategory> categoryOpt = helpService.getAllCategories().stream()
                .filter(c -> c.getId().equals(categoryId))
                .findFirst();

        if (categoryOpt.isEmpty()) {
            showCategoryOverview();
            return;
        }

        HelpCategory category = categoryOpt.get();

        // Show category header
        categoryHeader.setVisible(true);
        categoryHeader.setManaged(true);
        categoryIcon.setText(category.getIcon());
        categoryTitle.setText(category.getName());
        categoryDesc.setText(category.getDescription());

        // Update breadcrumb
        setBreadcrumb("Help Center", category.getName(), null);

        // Load articles
        articleContainer.getChildren().clear();
        articleContainer.getChildren().add(categoryHeader);

        List<HelpArticle> articles = helpService.getArticlesByCategory(categoryId);

        if (articles.isEmpty()) {
            Label noArticles = new Label("No articles in this category yet.");
            noArticles.getStyleClass().add("help-no-content");
            articleContainer.getChildren().add(noArticles);
        } else {
            for (HelpArticle article : articles) {
                VBox articleCard = createArticleCard(article);
                articleContainer.getChildren().add(articleCard);
            }
        }

        showView(guidesView);
    }

    private VBox createArticleCard(HelpArticle article) {
        VBox card = new VBox(4);
        card.getStyleClass().add("help-article-card");
        card.setPadding(new Insets(12, 16, 12, 16));

        Label titleLabel = new Label(article.getTitle());
        titleLabel.getStyleClass().add("help-article-title");

        // Preview text (first 100 chars of content)
        String preview = article.getContent()
                .replaceAll("#.*\\n", "")  // Remove markdown headers
                .replaceAll("\\*\\*", "")   // Remove bold
                .replaceAll("\\n+", " ")    // Replace newlines with spaces
                .trim();
        if (preview.length() > 150) {
            preview = preview.substring(0, 150) + "...";
        }

        Label previewLabel = new Label(preview);
        previewLabel.getStyleClass().add("help-article-preview");
        previewLabel.setWrapText(true);

        // Tags
        if (article.getTags() != null && !article.getTags().isEmpty()) {
            HBox tagsBox = new HBox(4);
            for (String tag : article.getTags().stream().limit(3).collect(Collectors.toList())) {
                Label tagLabel = new Label(tag);
                tagLabel.getStyleClass().add("help-tag");
                tagsBox.getChildren().add(tagLabel);
            }
            card.getChildren().addAll(titleLabel, previewLabel, tagsBox);
        } else {
            card.getChildren().addAll(titleLabel, previewLabel);
        }

        card.setOnMouseClicked(e -> showArticleDetail(article));

        return card;
    }

    private void showArticleDetail(HelpArticle article) {
        navigationStack.push("category:" + currentCategoryId);

        currentArticleId = article.getId();

        // Find category for breadcrumb
        Optional<HelpCategory> categoryOpt = helpService.getAllCategories().stream()
                .filter(c -> c.getId().equals(article.getCategoryId()))
                .findFirst();

        String categoryName = categoryOpt.map(HelpCategory::getName).orElse("Guides");

        // Update breadcrumb
        setBreadcrumb("Help Center", categoryName, article.getTitle());

        // Render article content
        articleDetailContainer.getChildren().clear();

        // Title
        Label titleLabel = new Label(article.getTitle());
        titleLabel.getStyleClass().add("help-detail-title");

        articleDetailContainer.getChildren().add(titleLabel);

        // Render markdown-like content
        VBox contentBox = renderMarkdownContent(article.getContent());
        articleDetailContainer.getChildren().add(contentBox);

        // Tags
        if (article.getTags() != null && !article.getTags().isEmpty()) {
            HBox tagsBox = new HBox(8);
            tagsBox.setPadding(new Insets(24, 0, 0, 0));
            Label tagsLabel = new Label("Tags: ");
            tagsLabel.getStyleClass().add("help-tags-label");
            tagsBox.getChildren().add(tagsLabel);

            for (String tag : article.getTags()) {
                Label tagLabel = new Label(tag);
                tagLabel.getStyleClass().add("help-tag");
                tagsBox.getChildren().add(tagLabel);
            }
            articleDetailContainer.getChildren().add(tagsBox);
        }

        showView(articleDetailView);
    }

    // ========================================================================
    // GLOSSARY
    // ========================================================================

    private void loadAlphabetInSidebar() {
        categoryList.getChildren().clear();

        // Add "All" button
        Button allBtn = new Button("All Terms");
        allBtn.getStyleClass().add("help-category-button");
        allBtn.setMaxWidth(Double.MAX_VALUE);
        allBtn.setOnAction(e -> loadGlossaryTerms(null));
        categoryList.getChildren().add(allBtn);

        // Add alphabet buttons
        for (char c = 'A'; c <= 'Z'; c++) {
            final char letter = c;
            Button btn = new Button(String.valueOf(c));
            btn.getStyleClass().add("help-letter-button");
            btn.setOnAction(e -> loadGlossaryTerms(letter));
            categoryList.getChildren().add(btn);
        }
    }

    private void loadGlossaryTerms(Character letter) {
        glossaryContainer.getChildren().clear();

        List<GlossaryTerm> terms;
        if (letter == null) {
            terms = helpService.getAllGlossaryTerms();
        } else {
            terms = helpService.getGlossaryByLetter(letter);
            setBreadcrumb("Help Center", "Glossary", "Letter " + letter);
        }

        if (terms.isEmpty()) {
            Label noTerms = new Label(letter == null ?
                    "No glossary terms available." :
                    "No terms starting with '" + letter + "'.");
            noTerms.getStyleClass().add("help-no-content");
            glossaryContainer.getChildren().add(noTerms);
            return;
        }

        for (GlossaryTerm term : terms) {
            VBox termCard = createGlossaryCard(term);
            glossaryContainer.getChildren().add(termCard);
        }
    }

    private VBox createGlossaryCard(GlossaryTerm term) {
        VBox card = new VBox(8);
        card.getStyleClass().add("help-glossary-card");
        card.setPadding(new Insets(12, 16, 12, 16));

        Label termLabel = new Label(term.getTerm());
        termLabel.getStyleClass().add("help-glossary-term");

        Label defLabel = new Label(term.getDefinition());
        defLabel.getStyleClass().add("help-glossary-def");
        defLabel.setWrapText(true);

        card.getChildren().addAll(termLabel, defLabel);

        // Related terms
        if (term.getRelatedTerms() != null && !term.getRelatedTerms().isEmpty()) {
            HBox relatedBox = new HBox(4);
            relatedBox.setAlignment(Pos.CENTER_LEFT);
            Label seeAlso = new Label("See also: ");
            seeAlso.getStyleClass().add("help-see-also");
            relatedBox.getChildren().add(seeAlso);

            for (String related : term.getRelatedTerms()) {
                Hyperlink link = new Hyperlink(related);
                link.getStyleClass().add("help-related-link");
                // Could add action to navigate to related term
                relatedBox.getChildren().add(link);
            }
            card.getChildren().add(relatedBox);
        }

        return card;
    }

    // ========================================================================
    // SHORTCUTS
    // ========================================================================

    private void loadShortcutCategoriesInSidebar() {
        categoryList.getChildren().clear();

        List<String> categories = helpService.getAllShortcuts().stream()
                .map(KeyboardShortcut::getCategory)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        // Add "All" button
        Button allBtn = new Button("All Shortcuts");
        allBtn.getStyleClass().add("help-category-button");
        allBtn.setMaxWidth(Double.MAX_VALUE);
        allBtn.setOnAction(e -> loadAllShortcuts());
        categoryList.getChildren().add(allBtn);

        for (String category : categories) {
            Button btn = new Button(category);
            btn.getStyleClass().add("help-category-button");
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setOnAction(e -> loadShortcutsByCategory(category));
            categoryList.getChildren().add(btn);
        }
    }

    private void loadAllShortcuts() {
        shortcutsContainer.getChildren().clear();

        // Group by category
        Map<String, List<KeyboardShortcut>> grouped = helpService.getAllShortcuts().stream()
                .collect(Collectors.groupingBy(KeyboardShortcut::getCategory));

        for (String category : grouped.keySet().stream().sorted().collect(Collectors.toList())) {
            VBox categoryBox = createShortcutCategoryBox(category, grouped.get(category));
            shortcutsContainer.getChildren().add(categoryBox);
        }
    }

    private void loadShortcutsByCategory(String category) {
        shortcutsContainer.getChildren().clear();

        List<KeyboardShortcut> shortcuts = helpService.getShortcutsByCategory(category);
        VBox categoryBox = createShortcutCategoryBox(category, shortcuts);
        shortcutsContainer.getChildren().add(categoryBox);

        setBreadcrumb("Help Center", "Keyboard Shortcuts", category);
    }

    private VBox createShortcutCategoryBox(String category, List<KeyboardShortcut> shortcuts) {
        VBox box = new VBox(8);

        Label categoryLabel = new Label(category);
        categoryLabel.getStyleClass().add("help-shortcut-category");

        box.getChildren().add(categoryLabel);

        // Create table-like layout
        GridPane grid = new GridPane();
        grid.setHgap(24);
        grid.setVgap(8);
        grid.getStyleClass().add("help-shortcut-grid");

        int row = 0;
        for (KeyboardShortcut shortcut : shortcuts) {
            Label keysLabel = new Label(shortcut.getKeys());
            keysLabel.getStyleClass().add("help-shortcut-keys");

            Label actionLabel = new Label(shortcut.getAction());
            actionLabel.getStyleClass().add("help-shortcut-action");

            grid.add(keysLabel, 0, row);
            grid.add(actionLabel, 1, row);
            row++;
        }

        box.getChildren().add(grid);

        return box;
    }

    // ========================================================================
    // SEARCH RESULTS
    // ========================================================================

    private void showSearchResults(SearchResults results) {
        searchResultsContainer.getChildren().clear();

        int totalResults = results.getArticles().size() + results.getGlossaryTerms().size();
        searchResultsTitle.setText("Search Results for \"" + results.getQuery() + "\"");
        searchResultsCount.setText(totalResults + " result(s) found");

        if (totalResults == 0) {
            Label noResults = new Label("No results found. Try different keywords.");
            noResults.getStyleClass().add("help-no-content");
            searchResultsContainer.getChildren().add(noResults);
        } else {
            // Show articles
            if (!results.getArticles().isEmpty()) {
                Label articlesLabel = new Label("Help Articles");
                articlesLabel.getStyleClass().add("help-results-section");
                searchResultsContainer.getChildren().add(articlesLabel);

                for (HelpArticle article : results.getArticles()) {
                    VBox card = createArticleCard(article);
                    searchResultsContainer.getChildren().add(card);
                }
            }

            // Show glossary terms
            if (!results.getGlossaryTerms().isEmpty()) {
                Label glossaryLabel = new Label("Glossary Terms");
                glossaryLabel.getStyleClass().add("help-results-section");
                glossaryLabel.setPadding(new Insets(16, 0, 0, 0));
                searchResultsContainer.getChildren().add(glossaryLabel);

                for (GlossaryTerm term : results.getGlossaryTerms()) {
                    VBox card = createGlossaryCard(term);
                    searchResultsContainer.getChildren().add(card);
                }
            }
        }

        setBreadcrumb("Help Center", "Search", "\"" + results.getQuery() + "\"");
        showView(searchResultsView);
    }

    // ========================================================================
    // HELPER METHODS
    // ========================================================================

    private void setActiveNavButton(Button activeBtn) {
        btnGuides.getStyleClass().remove("help-nav-active");
        btnGlossary.getStyleClass().remove("help-nav-active");
        btnShortcuts.getStyleClass().remove("help-nav-active");

        activeBtn.getStyleClass().add("help-nav-active");
    }

    private void showView(VBox viewToShow) {
        guidesView.setVisible(false);
        guidesView.setManaged(false);
        glossaryView.setVisible(false);
        glossaryView.setManaged(false);
        shortcutsView.setVisible(false);
        shortcutsView.setManaged(false);
        searchResultsView.setVisible(false);
        searchResultsView.setManaged(false);
        articleDetailView.setVisible(false);
        articleDetailView.setManaged(false);

        viewToShow.setVisible(true);
        viewToShow.setManaged(true);
    }

    private void setBreadcrumb(String level1, String level2, String level3) {
        breadcrumb1.setText(level1);

        if (level2 != null) {
            breadcrumbSep1.setVisible(true);
            breadcrumb2.setVisible(true);
            breadcrumb2.setText(level2);
        } else {
            breadcrumbSep1.setVisible(false);
            breadcrumb2.setVisible(false);
        }

        if (level3 != null) {
            breadcrumbSep2.setVisible(true);
            breadcrumb3.setVisible(true);
            breadcrumb3.setText(level3);
        } else {
            breadcrumbSep2.setVisible(false);
            breadcrumb3.setVisible(false);
        }
    }

    private VBox renderMarkdownContent(String content) {
        VBox container = new VBox(8);

        String[] lines = content.split("\\n");
        StringBuilder currentParagraph = new StringBuilder();

        for (String line : lines) {
            if (line.startsWith("# ")) {
                // H1 header
                flushParagraph(container, currentParagraph);
                Label header = new Label(line.substring(2));
                header.getStyleClass().add("help-content-h1");
                container.getChildren().add(header);
            } else if (line.startsWith("## ")) {
                // H2 header
                flushParagraph(container, currentParagraph);
                Label header = new Label(line.substring(3));
                header.getStyleClass().add("help-content-h2");
                container.getChildren().add(header);
            } else if (line.startsWith("### ")) {
                // H3 header
                flushParagraph(container, currentParagraph);
                Label header = new Label(line.substring(4));
                header.getStyleClass().add("help-content-h3");
                container.getChildren().add(header);
            } else if (line.startsWith("- ") || line.startsWith("* ")) {
                // Bullet point
                flushParagraph(container, currentParagraph);
                HBox bulletBox = new HBox(8);
                bulletBox.setAlignment(Pos.TOP_LEFT);
                Label bullet = new Label("•");
                bullet.getStyleClass().add("help-bullet");
                Label text = new Label(processBoldText(line.substring(2)));
                text.getStyleClass().add("help-content-text");
                text.setWrapText(true);
                bulletBox.getChildren().addAll(bullet, text);
                HBox.setHgrow(text, Priority.ALWAYS);
                container.getChildren().add(bulletBox);
            } else if (line.startsWith("|")) {
                // Table row - simplified rendering
                flushParagraph(container, currentParagraph);
                if (!line.contains("---")) {  // Skip separator rows
                    Label tableRow = new Label(line.replaceAll("\\|", "    "));
                    tableRow.getStyleClass().add("help-table-row");
                    container.getChildren().add(tableRow);
                }
            } else if (line.startsWith("```")) {
                // Code block marker - toggle
                flushParagraph(container, currentParagraph);
            } else if (line.trim().isEmpty()) {
                // Empty line - paragraph break
                flushParagraph(container, currentParagraph);
            } else {
                // Regular text
                if (currentParagraph.length() > 0) {
                    currentParagraph.append(" ");
                }
                currentParagraph.append(line);
            }
        }

        // Flush remaining text
        flushParagraph(container, currentParagraph);

        return container;
    }

    private void flushParagraph(VBox container, StringBuilder paragraph) {
        if (paragraph.length() > 0) {
            Label text = new Label(processBoldText(paragraph.toString()));
            text.getStyleClass().add("help-content-text");
            text.setWrapText(true);
            container.getChildren().add(text);
            paragraph.setLength(0);
        }
    }

    private String processBoldText(String text) {
        // Simple bold text handling - just remove ** markers for now
        // A more sophisticated implementation would use TextFlow with styled Text nodes
        return text.replaceAll("\\*\\*([^*]+)\\*\\*", "$1");
    }
}
