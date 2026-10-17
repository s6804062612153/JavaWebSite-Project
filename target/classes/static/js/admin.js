'use strict';

const $ = (id) => document.getElementById(id);
const LETTERS = ['A', 'B', 'C', 'D'];
const CATEGORY_LABELS = { ANIME_GAMING: 'Anime & Gaming', GENERAL: 'General Knowledge' };
const DIFFICULTY_LABELS = { EASY: 'ง่าย · Easy', MEDIUM: 'ปานกลาง · Medium', HARD: 'ยาก · Hard' };
const state = { page: 0, size: 20, totalPages: 0, totalElements: 0 };

function escapeHtml(value) {
    return String(value ?? '').replaceAll('&', '&amp;').replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;').replaceAll('"', '&quot;').replaceAll("'", '&#039;');
}

function categoryLabel(category) { return CATEGORY_LABELS[category] || category || 'ไม่ระบุ'; }
function difficultyLabel(difficulty) { return DIFFICULTY_LABELS[difficulty] || difficulty || 'ไม่ระบุ'; }
function difficultyClass(difficulty) { return `badge-${String(difficulty || '').toLowerCase()}`; }
function setMessage(id, text, success = false) {
    const element = $(id);
    element.textContent = text;
    element.classList.toggle('success', success);
}

async function api(path) {
    const response = await fetch(path, {
        headers: { 'Accept': 'application/json' },
        cache: 'no-store'
    });
    if (!response.ok) {
        let detail = '';
        try {
            const body = await response.json();
            detail = body.message || body.error || '';
        } catch { /* response may not contain JSON */ }
        throw new Error(detail || `เชื่อมต่อ API ไม่สำเร็จ (${response.status})`);
    }
    return response.json();
}

function renderSummary(summary) {
    const categories = summary.categories || {};
    const metrics = [
        { label: 'คำถามทั้งหมด', value: summary.totalQuestions ?? 0, foot: 'รายการในฐานข้อมูล', icon: '▤' },
        { label: 'Anime & Gaming', value: categories.ANIME_GAMING ?? 0, foot: 'คำถามในหมวดนี้', icon: '✦' },
        { label: 'General Knowledge', value: categories.GENERAL ?? 0, foot: 'คำถามในหมวดนี้', icon: '◎' },
        { label: 'หมวดหมู่', value: Object.values(categories).filter((count) => Number(count) > 0).length, foot: 'หมวดที่มีคำถามอย่างน้อย 1 ข้อ', icon: '◫' }
    ];
    $('summaryCards').innerHTML = metrics.map((metric) => `
        <article class="metric-card"><div class="metric-top"><span class="metric-label">${escapeHtml(metric.label)}</span><span class="metric-icon">${escapeHtml(metric.icon)}</span></div>
        <div class="metric-value">${Number(metric.value).toLocaleString('th-TH')}</div><div class="metric-foot">${escapeHtml(metric.foot)}</div></article>
    `).join('');

    const diffs = summary.difficulties || {};
    const matrix = summary.categoryDifficulty || {};
    const rows = Object.keys(categories).map((category) => `
        <tr><td><strong>${escapeHtml(categoryLabel(category))}</strong></td>
        ${['EASY', 'MEDIUM', 'HARD'].map((difficulty) => `<td class="num">${Number(matrix[category]?.[difficulty] ?? 0).toLocaleString('th-TH')}</td>`).join('')}
        <td class="num total">${Number(categories[category] ?? 0).toLocaleString('th-TH')}</td></tr>`).join('');
    $('distributionTable').innerHTML = `
        <table><thead><tr><th>หมวดหมู่</th><th class="num">ง่าย · Easy</th><th class="num">ปานกลาง · Medium</th><th class="num">ยาก · Hard</th><th class="num">รวม</th></tr></thead>
        <tbody>${rows}<tr class="total-row"><td>รวมทุกหมวด</td>${['EASY', 'MEDIUM', 'HARD'].map((difficulty) => `<td class="num">${Number(diffs[difficulty] ?? 0).toLocaleString('th-TH')}</td>`).join('')}<td class="num total">${Number(summary.totalQuestions ?? 0).toLocaleString('th-TH')}</td></tr></tbody></table>`;
}

function buildQuestionCard(question) {
    const options = Array.isArray(question.options) ? question.options : [];
    const optionsHtml = options.map((option, index) => {
        const correct = index === Number(question.correctIndex);
        return `<div class="option-card ${correct ? 'correct' : ''}"><span class="option-letter">${LETTERS[index] || index + 1}</span><span>${escapeHtml(option)}${correct ? ' ✓' : ''}</span></div>`;
    }).join('');
    const explanation = String(question.explanation || '').trim();
    return `<article class="question-item">
        <div class="question-main"><div class="question-id">#${Number(question.id)}</div>
            <div class="question-copy"><div class="badges"><span class="badge badge-category">${escapeHtml(categoryLabel(question.category))}</span><span class="badge ${difficultyClass(question.difficulty)}">${escapeHtml(difficultyLabel(question.difficulty))}</span></div>
                <p class="question-text">${escapeHtml(question.questionText)}</p></div>
            <button class="details-toggle" type="button" aria-expanded="false">ดูเฉลย +</button>
        </div>
        <div class="question-details" hidden><div class="options-grid">${optionsHtml}</div>
            <p class="answer-label">คำตอบที่ถูกต้อง: ${escapeHtml(question.correctAnswer || (options[question.correctIndex] ?? 'ไม่ระบุ'))}</p>
            <p class="explanation">${explanation ? `<strong>คำอธิบาย:</strong> ${escapeHtml(explanation)}` : 'ยังไม่มีคำอธิบายสำหรับข้อนี้'}</p></div>
    </article>`;
}

async function loadQuestions() {
    const params = new URLSearchParams({
        category: $('categoryFilter').value,
        difficulty: $('difficultyFilter').value,
        keyword: $('keywordFilter').value.trim(),
        page: String(state.page),
        size: String(state.size)
    });
    $('questionList').innerHTML = '<div class="empty-state">กำลังโหลดรายการคำถาม...</div>';
    $('resultMeta').textContent = 'กำลังค้นหา...';
    try {
        const data = await api(`/api/admin/questions?${params.toString()}`);
        state.totalPages = Number(data.totalPages || 0);
        state.totalElements = Number(data.totalElements || 0);
        const questions = Array.isArray(data.content) ? data.content : [];
        $('questionList').innerHTML = questions.length
            ? questions.map(buildQuestionCard).join('')
            : '<div class="empty-state">ไม่พบคำถามที่ตรงกับเงื่อนไข</div>';
        $('resultMeta').textContent = `พบ ${state.totalElements.toLocaleString('th-TH')} ข้อ · แสดง ${questions.length ? (state.page * state.size + 1).toLocaleString('th-TH') : 0}–${(state.page * state.size + questions.length).toLocaleString('th-TH')}`;
        const pageShown = state.totalPages === 0 ? 0 : state.page + 1;
        $('pageInfo').textContent = `หน้า ${pageShown.toLocaleString('th-TH')} / ${state.totalPages.toLocaleString('th-TH')}`;
        $('prevPage').disabled = state.page <= 0;
        $('nextPage').disabled = state.totalPages === 0 || state.page >= state.totalPages - 1;
        setMessage('dashboardMessage', '');
        return true;
    } catch (error) {
        $('questionList').innerHTML = `<div class="empty-state">${escapeHtml(error.message)}</div>`;
        setMessage('dashboardMessage', error.message || 'โหลดคำถามไม่สำเร็จ');
        return false;
    }
}

async function refreshAll() {
    try {
        const summary = await api('/api/admin/summary');
        renderSummary(summary);
        if (await loadQuestions()) setMessage('dashboardMessage', 'อัปเดตข้อมูลล่าสุดแล้ว', true);
    } catch (error) {
        setMessage('dashboardMessage', error.message || 'รีเฟรชไม่สำเร็จ');
    }
}

$('refreshButton').addEventListener('click', refreshAll);
$('filterForm').addEventListener('submit', async (event) => {
    event.preventDefault();
    state.page = 0;
    state.size = Number($('pageSize').value) || 20;
    await loadQuestions();
});
$('resetFilters').addEventListener('click', async () => {
    $('keywordFilter').value = '';
    $('categoryFilter').value = 'ALL';
    $('difficultyFilter').value = 'ALL';
    $('pageSize').value = '20';
    state.page = 0;
    state.size = 20;
    await loadQuestions();
});
$('prevPage').addEventListener('click', async () => {
    if (state.page > 0) { state.page -= 1; await loadQuestions(); }
});
$('nextPage').addEventListener('click', async () => {
    if (state.page < state.totalPages - 1) { state.page += 1; await loadQuestions(); }
});
$('questionList').addEventListener('click', (event) => {
    const button = event.target.closest('.details-toggle');
    if (!button) return;
    const details = button.closest('.question-item')?.querySelector('.question-details');
    if (!details) return;
    const opening = details.hidden;
    details.hidden = !opening;
    button.textContent = opening ? 'ซ่อนเฉลย −' : 'ดูเฉลย +';
    button.setAttribute('aria-expanded', String(opening));
});


// Load the read-only dashboard automatically; no password or environment key is required.
refreshAll();
