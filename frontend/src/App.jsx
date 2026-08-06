import { useEffect, useState } from "react";
import "./App.css";

const movieSuggestions = [
  "Interstellar",
  "Inception",
  "Harry Potter",
  "Avatar",
];

const fallbackThemes = [
  "Similar genre",
  "Related atmosphere",
  "Comparable characters",
  "Shared story themes",
  "Matching reading experience",
];

function App() {
  const [query, setQuery] = useState("Interstellar");
  const [selectedStory, setSelectedStory] = useState(null);
  const [books, setBooks] = useState([]);

  const [loading, setLoading] = useState(false);
  const [ratingBookId, setRatingBookId] = useState(null);
  const [error, setError] = useState("");

  const [theme, setTheme] = useState("dark");
  const [activeType, setActiveType] = useState("movie");

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", theme);
  }, [theme]);

  async function searchBooks(searchValue = query) {
    const cleanValue = searchValue.trim();

    if (!cleanValue) {
      setError("Enter a movie title.");
      setSelectedStory(null);
      setBooks([]);
      return;
    }

    if (activeType === "game") {
      setError("Game search is not connected yet. Use Movie for now.");
      setSelectedStory(null);
      setBooks([]);
      return;
    }

    setLoading(true);
    setError("");

    try {
      const response = await fetch(
          `http://localhost:8080/api/recommendations/full?movie=${encodeURIComponent(
              cleanValue
          )}`
      );

      if (!response.ok) {
        const errorData = await response.json().catch(() => null);

        throw new Error(
            errorData?.message ||
            `Could not load recommendations for "${cleanValue}".`
        );
      }

      const data = await response.json();

      setSelectedStory(data.source ?? null);

      const recommendations = Array.isArray(data.recommendations)
          ? data.recommendations
          : [];

      setBooks(recommendations);

      if (recommendations.length === 0) {
        setError(
            `No book recommendations found for "${
                data.source?.title || cleanValue
            }".`
        );
      }
    } catch (requestError) {
      setSelectedStory(null);
      setBooks([]);
      setError(requestError.message || "Something went wrong.");
    } finally {
      setLoading(false);
    }
  }

  async function rateBook(bookId, rating) {
    setRatingBookId(bookId);
    setError("");

    try {
      const response = await fetch(
          `http://localhost:8080/api/books/${bookId}/rating?rating=${rating}`,
          {
            method: "PUT",
          }
      );

      if (!response.ok) {
        const errorData = await response.json().catch(() => null);

        throw new Error(
            errorData?.message || "Could not save the rating."
        );
      }

      const updatedBook = await response.json();

      setBooks((currentBooks) =>
          currentBooks.map((book) =>
              book.id === updatedBook.id
                  ? {
                    ...book,
                    rating: updatedBook.rating,
                  }
                  : book
          )
      );
    } catch (requestError) {
      setError(requestError.message || "Could not save the rating.");
    } finally {
      setRatingBookId(null);
    }
  }

  function chooseSuggestion(value) {
    setQuery(value);
    searchBooks(value);
  }

  function selectType(type) {
    setActiveType(type);
    setError("");

    if (type === "game") {
      setSelectedStory(null);
      setBooks([]);
    }
  }

  const displayedThemes =
      selectedStory?.genres?.length > 0
          ? [
            ...selectedStory.genres.map(
                (genre) => `${genre} stories and atmosphere`
            ),
            "Comparable characters",
            "Shared narrative themes",
          ].slice(0, 5)
          : fallbackThemes;

  return (
      <div className="app-shell">
        <aside className="sidebar">
          <div>
            <div className="brand">
              <div className="brand-icon">📖</div>

              <div>
                <h2>
                  Book<span>Match</span>
                </h2>

                <p>AI-Powered Recommendations</p>
              </div>
            </div>

            <nav className="nav-menu">
              <button className="nav-item active">
                <span>⌂</span>
                Home
              </button>

              <button className="nav-item">
                <span>⌕</span>
                Search
              </button>

              <button
                  className={`nav-item ${
                      activeType === "movie" ? "active-type" : ""
                  }`}
                  onClick={() => selectType("movie")}
              >
                <span>🎬</span>
                Movies
              </button>

              <button
                  className={`nav-item ${
                      activeType === "game" ? "active-type" : ""
                  }`}
                  onClick={() => selectType("game")}
              >
                <span>🎮</span>
                Games
              </button>

              <button className="nav-item">
                <span>☆</span>
                Top Rated
              </button>

              <button className="nav-item">
                <span>ⓘ</span>
                About
              </button>
            </nav>
          </div>

          <div>
            <div className="sidebar-promo">
              <div className="promo-icon">✦</div>

              <h3>
                Discover books that match your favorite stories
              </h3>

              <p>
                Movies, games, and books — connected by AI.
              </p>
            </div>

            <div className="theme-switcher">
              <button
                  className={theme === "light" ? "selected" : ""}
                  onClick={() => setTheme("light")}
              >
                ☀ Light
              </button>

              <button
                  className={theme === "dark" ? "selected" : ""}
                  onClick={() => setTheme("dark")}
              >
                ☾ Dark
              </button>
            </div>

            <p className="copyright">
              © 2026 BookMatch AI
            </p>
          </div>
        </aside>

        <main className="main-content">
          <section className="hero">
            <div className="hero-copy">
              <h1>
                Find your next
                <br />
                favorite <span>book</span>
              </h1>

              <p>
                Get AI-powered book recommendations based on your
                favorite movies and games.
              </p>
            </div>

            <div className="search-panel">
              <div className="type-tabs">
                <button
                    className={activeType === "movie" ? "active" : ""}
                    onClick={() => selectType("movie")}
                >
                  🎬 Movie
                </button>

                <button
                    className={activeType === "game" ? "active" : ""}
                    onClick={() => selectType("game")}
                >
                  🎮 Game
                </button>
              </div>

              <div className="search-row">
                <div className="search-input-wrap">
                  <span>⌕</span>

                  <input
                      value={query}
                      onChange={(event) =>
                          setQuery(event.target.value)
                      }
                      onKeyDown={(event) => {
                        if (event.key === "Enter") {
                          searchBooks();
                        }
                      }}
                      placeholder={`Search for a ${activeType}...`}
                  />
                </div>

                <button
                    className="primary-button"
                    onClick={() => searchBooks()}
                    disabled={loading}
                >
                  {loading ? "Searching..." : "Search"}
                </button>
              </div>

              {activeType === "movie" && (
                  <div className="suggestions">
                    <span>Try:</span>

                    {movieSuggestions.map((suggestion) => (
                        <button
                            key={suggestion}
                            onClick={() =>
                                chooseSuggestion(suggestion)
                            }
                        >
                          {suggestion}
                        </button>
                    ))}
                  </div>
              )}
            </div>
          </section>

          <section className="dashboard-grid">
            <div className="recommendation-column">
              <div className="selected-story">
                <div className="story-poster">
                  {selectedStory?.posterUrl ? (
                      <img
                          src={selectedStory.posterUrl}
                          alt={`${selectedStory.title} poster`}
                      />
                  ) : (
                      <div className="poster-placeholder">
                        {activeType === "movie" ? "🎬" : "🎮"}
                      </div>
                  )}
                </div>

                <div className="story-content">
                  <p className="eyebrow">
                    Recommendations for
                  </p>

                  <h2>
                    {selectedStory?.title || "Search for a movie"}

                    {selectedStory?.year && (
                        <span> ({selectedStory.year})</span>
                    )}
                  </h2>

                  <div className="tag-row">
                    {selectedStory?.genres?.map((genre) => (
                        <span key={genre}>{genre}</span>
                    ))}
                  </div>

                  <p className="story-description">
                    {selectedStory?.description ||
                        "Search for a movie to see its information and matching books."}
                  </p>
                </div>

                <div className="ai-summary-card">
                  <h3>✦ Gemini AI Match</h3>

                  <p>
                    We found <strong>{books.length}</strong> books
                    that match the themes and atmosphere.
                  </p>
                </div>
              </div>

              {error && (
                  <p className="error-message">
                    {error}
                  </p>
              )}

              {loading && (
                  <div className="empty-state">
                    <span>⌛</span>

                    <h3>Analyzing with Gemini</h3>

                    <p>
                      Loading movie details and AI book matches...
                    </p>
                  </div>
              )}

              {!loading && books.length === 0 && !error && (
                  <div className="empty-state">
                    <span>📚</span>

                    <h3>Search for a story you love</h3>

                    <p>
                      Your recommended books will appear here.
                    </p>
                  </div>
              )}

              {!loading && books.length > 0 && (
                  <div className="recommendation-list">
                    {books.map((book) => {
                      const matchScore = Number(
                          book.matchScore ?? 80
                      );

                      const bookRating = Number(
                          book.rating ?? 0
                      );

                      const ratingLoading =
                          ratingBookId === book.id;

                      return (
                          <article
                              className="recommendation-card"
                              key={book.id}
                          >
                            <div className="book-cover">
                              {book.coverUrl ? (
                                  <img
                                      src={book.coverUrl}
                                      alt={`${book.title} cover`}
                                  />
                              ) : (
                                  <>
                                    <span>BOOKMATCH</span>
                                    <strong>{book.title}</strong>
                                  </>
                              )}
                            </div>

                            <div className="book-main">
                              <h3>{book.title}</h3>

                              <h4>{book.author}</h4>

                              <p>{book.description}</p>

                              {book.matchReason && (
                                  <div className="match-reason">
                                    <strong>
                                      Why it matches:
                                    </strong>

                                    <span>
                              {book.matchReason}
                            </span>
                                  </div>
                              )}

                              <div className="tag-row small">
                                {book.genre && (
                                    <span>{book.genre}</span>
                                )}

                                {book.tags
                                    ?.split(",")
                                    .map((tag) => tag.trim())
                                    .filter(Boolean)
                                    .slice(0, 3)
                                    .map((tag) => (
                                        <span
                                            key={`${book.id}-${tag}`}
                                        >
                                {tag}
                              </span>
                                    ))}
                              </div>
                            </div>

                            <div className="match-score">
                              <span>Gemini Match</span>

                              <strong>
                                {matchScore}%
                              </strong>
                            </div>

                            <div className="rating-area">
                              <div className="rating-line">
                          <span className="stars">
                            ★★★★★
                          </span>

                                <strong>
                                  {bookRating.toFixed(1)}
                                </strong>
                              </div>

                              <div className="rating-buttons">
                                {[1, 2, 3, 4, 5].map((rating) => (
                                    <button
                                        key={rating}
                                        onClick={() =>
                                            rateBook(book.id, rating)
                                        }
                                        disabled={ratingLoading}
                                        aria-label={`Rate ${book.title} ${rating} stars`}
                                    >
                                      {ratingLoading
                                          ? "..."
                                          : `${rating}★`}
                                    </button>
                                ))}
                              </div>
                            </div>
                          </article>
                      );
                    })}
                  </div>
              )}
            </div>

            <aside className="right-column">
              <section className="info-card">
                <h3>Why these books?</h3>

                <p>
                  Gemini compares genres, atmosphere, characters,
                  themes, and storytelling style.
                </p>

                <ul>
                  {displayedThemes.map((themeItem) => (
                      <li key={themeItem}>
                        {themeItem}
                      </li>
                  ))}
                </ul>
              </section>

              <section className="info-card">
                <h3>Rating Distribution</h3>

                <div className="rating-summary">
                  <strong>4.6</strong>
                  <span>★★★★★</span>
                </div>

                {[72, 20, 6, 2, 0].map(
                    (percentage, index) => (
                        <div
                            className="rating-bar-row"
                            key={`${percentage}-${index}`}
                        >
                          <span>{5 - index} ★</span>

                          <div className="rating-bar">
                            <div
                                style={{
                                  width: `${percentage}%`,
                                }}
                            />
                          </div>

                          <small>{percentage}%</small>
                        </div>
                    )
                )}
              </section>

              <section className="info-card">
                <div className="card-title-row">
                  <h3>Recent Searches</h3>
                  <button>View all</button>
                </div>

                <div className="recent-item">
                <span className="recent-icon">
                  {activeType === "movie" ? "🎬" : "🎮"}
                </span>

                  <div>
                    <strong>
                      {selectedStory?.title ||
                          "No recent search"}
                    </strong>

                    <small>
                      {selectedStory?.type || activeType}
                    </small>
                  </div>

                  <time>Now</time>
                </div>

                <div className="recent-item">
                <span className="recent-icon">
                  🎬
                </span>

                  <div>
                    <strong>Inception</strong>
                    <small>Movie</small>
                  </div>

                  <time>1 hour ago</time>
                </div>

                <div className="recent-item">
                <span className="recent-icon">
                  🎬
                </span>

                  <div>
                    <strong>Harry Potter</strong>
                    <small>Movie</small>
                  </div>

                  <time>3 hours ago</time>
                </div>
              </section>
            </aside>
          </section>
        </main>
      </div>
  );
}

export default App;