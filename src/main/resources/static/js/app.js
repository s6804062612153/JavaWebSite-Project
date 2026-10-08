/* QuizApp frontend — v1.2.0
 * Vanilla JS, no build step. Talks to the Spring Boot REST API (same origin).
 */
const VIEWS = ['setupView', 'quizView', 'resultView'];
const SESSION_KEY = 'quizapp-session';
const THEME_KEY = 'quizapp-theme';
const LETTERS = ['A', 'B', 'C', 'D'];
const RING_RADIUS = 52;
const RING_LENGTH = 2 * Math.PI * RING_RADIUS;

const state = {
    questions: [],
    answers: {},
    currentIndex: 0,
    settings: { category: 'ALL', difficulty: 'ALL', limit: 10 },
    result: null
};

const byId = (id) => document.getElementById(id);
const prefersReducedMotion = () => window.matchMedia('(prefers-reduced-motion: reduce)').matches;

/* ---------- View handling ---------- */

function canShow(id) {
    if (id === 'quizView') return state.questions.length > 0 && !state.result;
    if (id === 'resultView') return state.result !== null;
    return true;
}

function showView(id) {
    const target = canShow(id) ? id : 'setupView';
    VIEWS.forEach((viewId) => byId(viewId).classList.toggle('active', viewId === target));
    document.querySelectorAll('[data-view-link]').forEach((link) => {
        link.classList.toggle('current', link.dataset.viewLink === target);
    });
    byId(target).scrollIntoView({ behavior: prefersReducedMotion() ? 'auto' : 'smooth', block: 'start' });
    return target;
}

function goTo(id) {
    const shown = showView(id);
    history.replaceState(null, '', `#${shown}`);
}

function syncViewFromHash() {
    const hash = window.location.hash.replace('#', '');
    showView(VIEWS.includes(hash) ? hash : 'setupView');
}

/* ---------- Status / loading ---------- */

function setLoading(loading) {
    const overlay = byId('loadingOverlay');
    overlay.hidden = !loading;
    overlay.setAttribute('aria-busy', String(loading));
    byId('startButton').disabled = loading || !limitInfo.ready || limitInfo.max <= 0;
    byId('submitButton').disabled = loading;
}

function setApiStatus(type, text) {
    byId('apiStatusDot').className = `status-indicator ${type}`;
    byId('apiStatusText').textContent = text;
}

async function checkApi() {
    if (window.location.protocol === 'file:') {
        setApiStatus('error', 'กรุณาเปิดผ่าน http://localhost:8080');
        return;
    }
    try {
        const response = await fetch('/api/quiz/health', { cache: 'no-store' });
        if (!response.ok) throw new Error('API unavailable');
        const health = await response.json();
        setApiStatus('ok', `API พร้อมใช้งาน${health.version ? ` · v${health.version}` : ''}`);
    } catch {
        setApiStatus('error', 'API ไม่พร้อมใช้งาน');
    }
}

/* ---------- Session persistence (resume an unfinished quiz) ---------- */

function saveSession() {
    try {
        if (state.questions.length === 0 || state.result) {
            sessionStorage.removeItem(SESSION_KEY);
            return;
        }
        sessionStorage.setItem(SESSION_KEY, JSON.stringify({
            questions: state.questions,
            answers: state.answers,
            currentIndex: state.currentIndex,
            settings: state.settings
        }));
    } catch { /* storage unavailable — resume is optional */ }
}

function loadSession() {
    try {
        const raw = sessionStorage.getItem(SESSION_KEY);
        if (!raw) return false;
        const saved = JSON.parse(raw);
        if (!Array.isArray(saved.questions) || saved.questions.length === 0) return false;
        state.questions = saved.questions;
        state.answers = saved.answers ?? {};
        state.currentIndex = Math.min(Number(saved.currentIndex) || 0, saved.questions.length - 1);
        state.settings = saved.settings ?? state.settings;
        return true;
    } catch {
        return false;
    }
}

function clearSession() {
    try { sessionStorage.removeItem(SESSION_KEY); } catch { /* ignore */ }
}

function renderResumeBanner() {
    const banner = byId('resumeBanner');
    const hasUnfinished = state.questions.length > 0 && !state.result;
    banner.hidden = !hasUnfinished;
    if (hasUnfinished) {
        byId('resumeText').textContent =
            `มีแบบทดสอบที่ทำค้างไว้ — ตอบแล้ว ${answeredCount()} / ${state.questions.length} ข้อ`;
    }
}

/* ---------- Setup ---------- */

/* ---------- Question count picker ---------- */

const HARD_MAX_LIMIT = 20;
const PRESET_VALUES = [5, 10, 15, 20];
const limitInfo = { max: HARD_MAX_LIMIT, available: null, ready: false };
let hintTimer = null;
let countRequestId = 0;

function clampLimit(value) {
    const number = Math.round(Number(value));
    if (!Number.isFinite(number)) return Math.min(10, limitInfo.max);
    return Math.max(1, Math.min(number, Math.max(1, limitInfo.max)));
}

function setLimit(value) {
    const limit = clampLimit(value);
    byId('limit').value = limit;
    byId('limitRange').value = limit;
    byId('limitMinus').disabled = limit <= 1;
    byId('limitPlus').disabled = limit >= limitInfo.max;
    byId('limitPresets').querySelectorAll('.preset').forEach((button) => {
        button.setAttribute('aria-checked', String(Number(button.dataset.value) === limit));
    });
}

function renderPresets() {
    const values = PRESET_VALUES.filter((value) => value < limitInfo.max);
    if (limitInfo.max > 0) values.push(limitInfo.max);   // last chip = "all available"
    const unique = [...new Set(values)];

    byId('limitPresets').innerHTML = unique.map((value) => {
        const isAll = value === limitInfo.max && !PRESET_VALUES.includes(value);
        const label = isAll ? `ทั้งหมด (${value})` : String(value);
        return `<button type="button" class="preset" role="radio" aria-checked="false" data-value="${value}">${label}</button>`;
    }).join('');
}

function applyLimitBounds() {
    const max = Math.max(limitInfo.max, 0);
    const hasQuestions = max > 0;
    ['limit', 'limitRange'].forEach((id) => {
        byId(id).max = String(Math.max(max, 1));
        byId(id).disabled = !hasQuestions;
    });
    byId('limitMaxLabel').textContent = hasQuestions ? `${max} questions` : '0 questions';
    byId('startButton').disabled = !limitInfo.ready || !hasQuestions;

    const hint = byId('availableHint');
    if (!hasQuestions) {
        hint.textContent = 'ไม่มีคำถามตามเงื่อนไขนี้';
        hint.classList.add('warn');
    } else {
        hint.textContent = `มีคำถามให้เลือก ${limitInfo.available ?? max} ข้อ`;
        hint.classList.remove('warn');
    }

    renderPresets();
    setLimit(hasQuestions ? byId('limit').value : 1);
}

async function refreshAvailableCount() {
    const requestId = ++countRequestId;
    limitInfo.ready = false;
    byId('startButton').disabled = true;
    byId('availableHint').textContent = 'กำลังตรวจสอบจำนวนข้อ...';
    byId('availableHint').classList.remove('warn');

    const params = new URLSearchParams({
        category: byId('category').value,
        difficulty: byId('difficulty').value
    });
    try {
        const response = await fetch(`/api/quiz/count?${params.toString()}`, { cache: 'no-store' });
        if (!response.ok) throw new Error('count unavailable');
        const data = await response.json();
        if (requestId !== countRequestId) return;   // a newer request superseded this one
        limitInfo.available = Number(data.available);
        limitInfo.max = Math.max(0, Math.min(Number(data.maxLimit), HARD_MAX_LIMIT));
        limitInfo.ready = true;
    } catch {
        if (requestId !== countRequestId) return;
        // Cannot verify how many questions exist -> do not allow a guess.
        limitInfo.available = null;
        limitInfo.max = 0;
        limitInfo.ready = false;
        applyLimitBounds();
        const hint = byId('availableHint');
        hint.textContent = 'ตรวจสอบจำนวนข้อไม่ได้ กรุณาตรวจสอบการเชื่อมต่อ API';
        hint.classList.add('warn');
        return;
    }
    applyLimitBounds();
}

function flashMaxHint() {
    const hint = byId('availableHint');
    hint.textContent = `เลือกได้สูงสุด ${limitInfo.max} ข้อ`;
    hint.classList.add('warn');
    clearTimeout(hintTimer);
    hintTimer = setTimeout(() => applyLimitBounds(), 1800);
}

async function startQuiz(event) {
    event?.preventDefault();
    byId('setupError').textContent = '';

    state.settings = {
        category: byId('category').value,
        difficulty: byId('difficulty').value,
        limit: clampLimit(byId('limit').value)
    };

    if (!limitInfo.ready || state.settings.limit > limitInfo.max) {
        byId('setupError').textContent = `จำนวนข้อต้องไม่เกิน ${limitInfo.max} ข้อ`;
        setLimit(limitInfo.max);
        return;
    }

    setLoading(true);
    try {
        const params = new URLSearchParams(state.settings);
        const response = await fetch(`/api/quiz/questions?${params.toString()}`);
        if (!response.ok) throw new Error(`ไม่สามารถโหลดคำถามได้ (HTTP ${response.status})`);

        const questions = await response.json();
        if (!Array.isArray(questions) || questions.length === 0) {
            throw new Error('ไม่พบคำถามตามเงื่อนไขที่เลือก');
        }

        state.questions = questions;
        state.answers = {};
        state.currentIndex = 0;
        state.result = null;
        saveSession();
        renderQuestion();
        goTo('quizView');
        setApiStatus('ok', 'API พร้อมใช้งาน');
    } catch (error) {
        byId('setupError').textContent = error.message || 'ไม่สามารถเริ่มแบบทดสอบได้';
        setApiStatus('error', 'เชื่อมต่อ API ไม่สำเร็จ');
    } finally {
        setLoading(false);
    }
}

/* ---------- Quiz ---------- */

const currentQuestion = () => state.questions[state.currentIndex];

function isAnswered(question) {
    const answer = state.answers[String(question.id)];
    return Number.isInteger(answer) && answer >= 0;
}

function answeredCount() {
    return state.questions.filter(isAnswered).length;
}

function renderQuestionNav() {
    byId('questionNav').innerHTML = state.questions.map((q, index) => {
        const classes = ['nav-dot'];
        if (index === state.currentIndex) classes.push('current');
        if (isAnswered(q)) classes.push('answered');
        const label = `ข้อ ${index + 1}${isAnswered(q) ? ' (ตอบแล้ว)' : ''}`;
        return `<button type="button" class="${classes.join(' ')}" data-jump="${index}"
                    aria-label="${label}" ${index === state.currentIndex ? 'aria-current="step"' : ''}>${index + 1}</button>`;
    }).join('');
}

function renderQuestion() {
    const q = currentQuestion();
    if (!q) return;

    const total = state.questions.length;
    const selected = state.answers[String(q.id)];
    const categoryLabel = q.category === 'ANIME_GAMING' ? 'ANIME & GAMING' : 'GENERAL';

    byId('questionCounter').textContent = `ข้อ ${state.currentIndex + 1} / ${total}`;
    byId('answeredCounter').textContent = `ตอบแล้ว ${answeredCount()}`;
    byId('progressBar').style.width = `${((state.currentIndex + 1) / total) * 100}%`;
    byId('questionNumberBadge').textContent = String(state.currentIndex + 1).padStart(2, '0');
    byId('questionCategory').textContent = categoryLabel;
    byId('questionDifficulty').textContent = q.difficulty;
    byId('quizTitle').textContent = `คำถามข้อที่ ${state.currentIndex + 1}`;
    byId('questionText').textContent = q.questionText;
    byId('quizError').textContent = '';

    byId('options').innerHTML = q.options.map((option, index) => `
        <button class="option ${selected === index ? 'selected' : ''}" type="button" role="radio"
                aria-checked="${selected === index}" data-index="${index}">
            <span>${escapeHtml(option)}</span>
            <span class="option-index">${LETTERS[index] ?? index + 1}</span>
        </button>
    `).join('');

    byId('prevButton').disabled = state.currentIndex === 0;
    byId('nextButton').hidden = state.currentIndex === total - 1;
    byId('submitButton').hidden = state.currentIndex !== total - 1;
    renderQuestionNav();
}

function selectOption(index) {
    const q = currentQuestion();
    if (!q || index < 0 || index >= q.options.length) return;
    state.answers[String(q.id)] = index;
    saveSession();
    renderQuestion();
}

function jumpTo(index) {
    if (index < 0 || index >= state.questions.length) return;
    state.currentIndex = index;
    saveSession();
    renderQuestion();
}

const nextQuestion = () => jumpTo(state.currentIndex + 1);
const prevQuestion = () => jumpTo(state.currentIndex - 1);

async function submitQuiz() {
    byId('quizError').textContent = '';
    if (state.questions.length === 0) return;

    const unanswered = state.questions.length - answeredCount();
    if (unanswered > 0 &&
        !window.confirm(`ยังไม่ได้ตอบ ${unanswered} ข้อ ต้องการส่งคำตอบเลยหรือไม่?`)) {
        return;
    }

    const payload = {
        category: state.settings.category,
        difficulty: state.settings.difficulty,
        userAnswers: Object.fromEntries(
            state.questions.map((q) => [String(q.id), isAnswered(q) ? state.answers[String(q.id)] : -1])
        )
    };

    setLoading(true);
    try {
        const response = await fetch('/api/quiz/submit', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (!response.ok) throw new Error(`HTTP ${response.status}`);

        state.result = await response.json();
        clearSession();
        renderResult(state.result);
        goTo('resultView');
    } catch {
        byId('quizError').textContent = 'ส่งคำตอบไม่สำเร็จ กรุณาลองอีกครั้ง';
    } finally {
        setLoading(false);
    }
}

/* ---------- Result ---------- */

function renderScoreRing(accuracy) {
    const clamped = Math.max(0, Math.min(100, accuracy));
    const ring = byId('scoreRingValue');
    ring.style.strokeDasharray = `${RING_LENGTH}`;
    ring.style.strokeDashoffset = `${RING_LENGTH * (1 - clamped / 100)}`;
    byId('scoreRingText').textContent = `${Math.round(clamped)}%`;
}

function renderResultStrip(reviews) {
    byId('resultStrip').innerHTML = reviews.map((review, index) => {
        const skipped = Number(review.userSelected) < 0;
        const status = review.correct ? 'correct' : skipped ? 'skipped' : 'incorrect';
        const label = review.correct ? 'ถูก' : skipped ? 'ไม่ได้ตอบ' : 'ผิด';
        return `<a class="strip-item ${status}" href="#review-${index}" data-review="${index}"
                   aria-label="ข้อ ${index + 1}: ${label}" title="ข้อ ${index + 1}: ${label}">${index + 1}</a>`;
    }).join('');
}

function renderResult(result) {
    const total = Number(result.totalQuestions ?? state.questions.length);
    const correct = Number(result.correctCount ?? 0);
    const accuracy = Number(result.accuracy ?? 0);

    byId('scoreValue').textContent = Number(result.score ?? 0);
    byId('correctValue').textContent = `${correct}/${total}`;
    byId('accuracyValue').textContent = `${accuracy.toFixed(1)}%`;
    byId('gradeBadge').textContent = result.grade ?? '-';
    byId('resultMessage').textContent = getResultMessage(accuracy);
    renderScoreRing(accuracy);

    const reviews = Array.isArray(result.reviews) ? result.reviews : [];
    renderResultStrip(reviews);

    byId('reviewList').innerHTML = reviews.map((review, index) => {
        const selected = Number(review.userSelected);
        const correctIndex = Number(review.correctIndex);
        const isCorrect = Boolean(review.correct);
        const options = Array.isArray(review.options) ? review.options : [];
        const status = isCorrect ? 'ถูกต้อง' : selected < 0 ? 'ไม่ได้ตอบ' : 'ไม่ถูกต้อง';

        const optionsHtml = options.map((option, optionIndex) => {
            const classes = ['review-option'];
            if (optionIndex === correctIndex) classes.push('correct');
            if (optionIndex === selected && optionIndex !== correctIndex) classes.push('user-wrong');
            const marker = optionIndex === correctIndex ? '✓ ' : optionIndex === selected ? '✕ ' : '';
            return `<div class="${classes.join(' ')}">${marker}${escapeHtml(option)}</div>`;
        }).join('');

        return `
            <article id="review-${index}" class="review-item ${isCorrect ? 'correct' : 'incorrect'}">
                <div class="review-top">
                    <div class="review-question">${index + 1}. ${escapeHtml(review.questionText)}</div>
                    <span class="review-status ${isCorrect ? 'correct' : 'incorrect'}">${status}</span>
                </div>
                <div class="review-options">${optionsHtml}</div>
                <div class="explanation"><strong>คำอธิบาย:</strong> ${escapeHtml(review.explanation || 'ไม่มีคำอธิบาย')}</div>
            </article>
        `;
    }).join('');
}

function getResultMessage(accuracy) {
    if (accuracy >= 90) return 'ยอดเยี่ยมมาก! ความรู้แน่นจริง 🔥';
    if (accuracy >= 80) return 'ทำได้ดีมาก! อีกนิดเดียวถึงระดับสูงสุด';
    if (accuracy >= 70) return 'ผลงานดีครับ ลองทบทวนข้อที่ผิดอีกนิด';
    if (accuracy >= 60) return 'ผ่านเกณฑ์แล้ว ลองทำอีกครั้งเพื่อเพิ่มคะแนน';
    return 'ไม่เป็นไรครับ รอบหน้ามีโอกาสทำได้ดีกว่าเดิม';
}

/* ---------- Helpers ---------- */

function escapeHtml(value) {
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}

function preferredTheme() {
    try {
        const saved = localStorage.getItem(THEME_KEY);
        if (saved === 'dark' || saved === 'light') return saved;
    } catch { /* ignore */ }
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
}

function applyTheme(theme) {
    document.documentElement.dataset.theme = theme;
    try { localStorage.setItem(THEME_KEY, theme); } catch { /* ignore */ }
    byId('themeIcon').textContent = theme === 'dark' ? '☀' : '☾';
    document.querySelector('meta[name="theme-color"]')
        ?.setAttribute('content', theme === 'dark' ? '#0f172a' : '#ffffff');
}

/* ---------- Events ---------- */

byId('quizForm').addEventListener('submit', startQuiz);
byId('limitRange').addEventListener('input', (event) => setLimit(event.target.value));
byId('limit').addEventListener('input', (event) => {
    // allow the field to be empty while typing; clamp on change/blur
    if (event.target.value === '') return;
    if (Number(event.target.value) > limitInfo.max) flashMaxHint();
    setLimit(event.target.value);
});
byId('limit').addEventListener('change', (event) => setLimit(event.target.value));
byId('limitMinus').addEventListener('click', () => setLimit(Number(byId('limit').value) - 1));
byId('limitPlus').addEventListener('click', () => setLimit(Number(byId('limit').value) + 1));
byId('limitPresets').addEventListener('click', (event) => {
    const button = event.target.closest('.preset');
    if (button) setLimit(Number(button.dataset.value));
});
byId('category').addEventListener('change', refreshAvailableCount);
byId('difficulty').addEventListener('change', refreshAvailableCount);
byId('nextButton').addEventListener('click', nextQuestion);
byId('prevButton').addEventListener('click', prevQuestion);
byId('submitButton').addEventListener('click', submitQuiz);
byId('backToSetup').addEventListener('click', () => { renderResumeBanner(); goTo('setupView'); });
byId('retryButton').addEventListener('click', () => startQuiz());
byId('resumeButton').addEventListener('click', () => { renderQuestion(); goTo('quizView'); });
byId('discardButton').addEventListener('click', () => {
    state.questions = [];
    state.answers = {};
    state.currentIndex = 0;
    clearSession();
    renderResumeBanner();
});
byId('themeToggle').addEventListener('click', () => {
    applyTheme(document.documentElement.dataset.theme === 'dark' ? 'light' : 'dark');
});

// Event delegation: options and question navigator are re-rendered on every change.
byId('options').addEventListener('click', (event) => {
    const button = event.target.closest('.option');
    if (button) selectOption(Number(button.dataset.index));
});
byId('questionNav').addEventListener('click', (event) => {
    const dot = event.target.closest('[data-jump]');
    if (dot) jumpTo(Number(dot.dataset.jump));
});
byId('resultStrip').addEventListener('click', (event) => {
    const item = event.target.closest('[data-review]');
    if (!item) return;
    event.preventDefault();
    byId(`review-${item.dataset.review}`)?.scrollIntoView({
        behavior: prefersReducedMotion() ? 'auto' : 'smooth',
        block: 'center'
    });
});

document.querySelectorAll('[data-view-link]').forEach((link) => {
    link.addEventListener('click', (event) => {
        event.preventDefault();
        goTo(link.dataset.viewLink);
    });
});

window.addEventListener('hashchange', syncViewFromHash);

document.addEventListener('keydown', (event) => {
    if (!byId('quizView').classList.contains('active')) return;
    if (event.ctrlKey || event.metaKey || event.altKey) return;
    if (event.target.closest('select, input, textarea')) return;

    if (['1', '2', '3', '4'].includes(event.key)) selectOption(Number(event.key) - 1);
    if (event.key === 'ArrowRight') nextQuestion();
    if (event.key === 'ArrowLeft') prevQuestion();
});

/* ---------- Init ---------- */

setLoading(false);
renderPresets();
setLimit(10);
refreshAvailableCount();
applyTheme(preferredTheme());
if (loadSession()) renderQuestion();
renderResumeBanner();
syncViewFromHash();
checkApi();
