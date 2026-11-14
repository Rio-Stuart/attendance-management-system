// app.js — Frontend Application Logic for Attendance Management System

let currentUser = null;
let currentRoster = [];
let currentActiveSession = null;
let currentPredictorData = null;

// API Base
const API_BASE = '/api';

// ---- INIT ----
document.addEventListener('DOMContentLoaded', () => {
  setupNavigation();
  setupModals();
  setupForms();

  // Try restoring session or default to Dr. Rajesh (Admin) for instant preview
  const saved = localStorage.getItem('ams_user');
  if (saved) {
    setUser(JSON.parse(saved));
  } else {
    // Auto-login as Dr. Rajesh (Admin) for convenient demo
    loginUser('admin1', 'admin123', 'ADMIN');
  }
});

// ---- NOTIFICATIONS ----
function showToast(message, type = 'success') {
  const toast = document.getElementById('toast');
  toast.className = `toast toast-${type}`;
  toast.textContent = message;
  toast.classList.remove('hidden');
  setTimeout(() => {
    toast.classList.add('hidden');
  }, 3500);
}

// ---- NAVIGATION & TABS ----
function setupNavigation() {
  // Tab switching
  document.querySelectorAll('.tab-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      const parent = btn.closest('.view-container') || document;
      parent.querySelectorAll('.tab-btn').forEach(b => b.classList.remove('active'));
      parent.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));

      btn.classList.add('active');
      const target = document.getElementById(btn.dataset.tab);
      if (target) target.classList.add('active');

      // Refresh data on tab activation
      if (btn.dataset.tab === 'sessionsTab') loadSessions();
      if (btn.dataset.tab === 'reportsTab') loadSectionReport();
      if (btn.dataset.tab === 'timetableTab') loadTimetable();
      if (btn.dataset.tab === 'studentDashboardTab' && currentUser) loadStudentDashboard();
      if (btn.dataset.tab === 'studentPredictorTab' && currentUser) initPredictor();
      if (btn.dataset.tab === 'studentTimetableTab' && currentUser) loadStudentTimetable();
    });
  });

  // Logout
  document.getElementById('logoutBtn').addEventListener('click', () => {
    localStorage.removeItem('ams_user');
    currentUser = null;
    updateUserUI();
    showLoginModal();
  });

  // Quick Login Buttons
  document.getElementById('quickLoginAdmin').addEventListener('click', () => {
    loginUser('admin1', 'admin123', 'ADMIN');
  });

  document.getElementById('quickLoginStudent').addEventListener('click', () => {
    loginUser('student1', 'student123', 'STUDENT');
  });
}

// ---- AUTHENTICATION ----
async function loginUser(username, password, role) {
  try {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    const result = await res.json();
    if (!result.success) throw new Error(result.message);

    setUser(result.user);
    localStorage.setItem('ams_user', JSON.stringify(result.user));
    hideLoginModal();
    showToast(`Signed in as ${result.user.full_name}`);
  } catch (err) {
    showToast(err.message, 'error');
  }
}

function setUser(user) {
  currentUser = user;
  updateUserUI();

  if (user.role === 'ADMIN') {
    document.getElementById('adminView').classList.remove('hidden');
    document.getElementById('studentView').classList.add('hidden');
    loadAdminInit();
  } else {
    document.getElementById('adminView').classList.add('hidden');
    document.getElementById('studentView').classList.remove('hidden');
    loadStudentDashboard();
    initPredictor();
    loadStudentTimetable();
  }
}

function updateUserUI() {
  const profileEl = document.getElementById('userProfile');
  const guestEl = document.getElementById('guestControls');

  if (currentUser) {
    profileEl.classList.remove('hidden');
    guestEl.classList.add('hidden');
    document.getElementById('userName').textContent = currentUser.full_name;
    document.getElementById('userAvatar').textContent = currentUser.full_name.charAt(0);
    const badge = document.getElementById('userRoleBadge');
    badge.textContent = currentUser.role;
    badge.className = `badge badge-${currentUser.role.toLowerCase()}`;
  } else {
    profileEl.classList.add('hidden');
    guestEl.classList.remove('hidden');
    document.getElementById('adminView').classList.add('hidden');
    document.getElementById('studentView').classList.add('hidden');
  }
}

// ---- MODALS & FORMS ----
function setupModals() {
  document.getElementById('closeLoginModal').addEventListener('click', hideLoginModal);
  document.getElementById('openCreateSessionBtn').addEventListener('click', () => {
    loadSectionsDropdown('modalSessionSection', true);
    document.getElementById('modalSessionDate').value = new Date().toISOString().split('T')[0];
    document.getElementById('createSessionModal').classList.remove('hidden');
  });
  document.getElementById('closeCreateSessionModal').addEventListener('click', () => {
    document.getElementById('createSessionModal').classList.add('hidden');
  });
  document.getElementById('cancelCreateSessionBtn').addEventListener('click', () => {
    document.getElementById('createSessionModal').classList.add('hidden');
  });

  // Generate modal
  document.getElementById('openGenerateModalBtn').addEventListener('click', () => {
    loadSectionsDropdown('modalGenSection');
    document.getElementById('generateModal').classList.remove('hidden');
  });
  document.getElementById('closeGenerateModal').addEventListener('click', () => {
    document.getElementById('generateModal').classList.add('hidden');
  });
  document.getElementById('cancelGenerateBtn').addEventListener('click', () => {
    document.getElementById('generateModal').classList.add('hidden');
  });

  // Cancel Session modal
  document.getElementById('closeCancelModal').addEventListener('click', () => {
    document.getElementById('cancelModal').classList.add('hidden');
  });
  document.getElementById('abortCancelBtn').addEventListener('click', () => {
    document.getElementById('cancelModal').classList.add('hidden');
  });

  // Edit Finalized Modal
  document.getElementById('closeEditModal').addEventListener('click', () => {
    document.getElementById('editFinalizedModal').classList.add('hidden');
  });
  document.getElementById('cancelEditBtn').addEventListener('click', () => {
    document.getElementById('editFinalizedModal').classList.add('hidden');
  });

  // Subject Session History Modal
  const closeHist = () => document.getElementById('subjectHistoryModal').classList.add('hidden');
  document.getElementById('closeSubjectHistoryModal')?.addEventListener('click', closeHist);
  document.getElementById('closeSubjectHistoryBtn')?.addEventListener('click', closeHist);
}

function showLoginModal() {
  document.getElementById('loginModal').classList.remove('hidden');
}
function hideLoginModal() {
  document.getElementById('loginModal').classList.add('hidden');
}
function fillLogin(u, p) {
  document.getElementById('loginUsername').value = u;
  document.getElementById('loginPassword').value = p;
}

function setupForms() {
  // Login form
  document.getElementById('loginForm').addEventListener('submit', e => {
    e.preventDefault();
    const u = document.getElementById('loginUsername').value.trim();
    const p = document.getElementById('loginPassword').value.trim();
    const r = document.querySelector('input[name="authRole"]:checked').value;
    loginUser(u, p, r);
  });

  // Create session form
  document.getElementById('createSessionForm').addEventListener('submit', async e => {
    e.preventDefault();
    try {
      const section_id = Number(document.getElementById('modalSessionSection').value);
      const section_subject_id = Number(document.getElementById('modalSessionSubject').value);
      const session_date = document.getElementById('modalSessionDate').value;
      const start_time = document.getElementById('modalSessionStart').value;
      const end_time = document.getElementById('modalSessionEnd').value;

      const res = await fetch(`${API_BASE}/sessions`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          section_id,
          section_subject_id,
          session_date,
          start_time,
          end_time,
          created_by: currentUser.id
        })
      });
      const data = await res.json();
      if (!data.success) throw new Error(data.message);

      showToast(data.message);
      document.getElementById('createSessionModal').classList.add('hidden');
      loadSessions();
    } catch (err) {
      showToast(err.message, 'error');
    }
  });

  // Subject cascade in create session
  document.getElementById('modalSessionSection').addEventListener('change', async e => {
    const sectionId = e.target.value;
    if (!sectionId) return;
    const res = await fetch(`${API_BASE}/sections/${sectionId}/subjects`);
    const data = await res.json();
    const select = document.getElementById('modalSessionSubject');
    select.innerHTML = data.data.map(ss => `<option value="${ss.section_subject_id}">${ss.subject_name} (${ss.subject_code})</option>`).join('');
  });

  // Generate sessions form
  document.getElementById('generateForm').addEventListener('submit', async e => {
    e.preventDefault();
    try {
      const sectionId = document.getElementById('modalGenSection').value;
      const daysAhead = document.getElementById('modalGenDays').value;

      const res = await fetch(`${API_BASE}/sections/${sectionId}/generate-timetable`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ daysAhead })
      });
      const data = await res.json();
      if (!data.success) throw new Error(data.message);

      showToast(data.message);
      document.getElementById('generateModal').classList.add('hidden');
      loadSessions();
    } catch (err) {
      showToast(err.message, 'error');
    }
  });

  // Confirm Cancel Form
  document.getElementById('cancelForm').addEventListener('submit', async e => {
    e.preventDefault();
    try {
      const id = document.getElementById('cancelSessionId').value;
      const reason = document.getElementById('cancelReasonInput').value;

      const res = await fetch(`${API_BASE}/sessions/${id}/cancel`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ reason })
      });
      const data = await res.json();
      if (!data.success) throw new Error(data.message);

      showToast(data.message);
      document.getElementById('cancelModal').classList.add('hidden');
      loadSessions();
    } catch (err) {
      showToast(err.message, 'error');
    }
  });

  // Edit finalized record form
  document.getElementById('editRecordForm').addEventListener('submit', async e => {
    e.preventDefault();
    try {
      const recordId = Number(document.getElementById('editRecordId').value);
      const newStatus = document.getElementById('editStatusSelect').value;
      const reason = document.getElementById('editReasonInput').value;

      const res = await fetch(`${API_BASE}/attendance/edit`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ recordId, newStatus, adminId: currentUser.id, reason })
      });
      const data = await res.json();
      if (!data.success) throw new Error(data.message);

      showToast(data.message);
      document.getElementById('editFinalizedModal').classList.add('hidden');
      if (currentActiveSession) loadRoster(currentActiveSession.id);
    } catch (err) {
      showToast(err.message, 'error');
    }
  });

  // Attendance Section Picker cascade
  document.getElementById('markingSectionSelect').addEventListener('change', async e => {
    const secId = e.target.value;
    const sessionSelect = document.getElementById('markingSessionSelect');
    const loadBtn = document.getElementById('loadRosterBtn');
    if (!secId) {
      sessionSelect.disabled = true;
      loadBtn.disabled = true;
      return;
    }
    const res = await fetch(`${API_BASE}/sessions?sectionId=${secId}`);
    const data = await res.json();
    sessionSelect.innerHTML = data.data.map(s => `
      <option value="${s.id}">#${s.id} &bull; ${s.session_date} ${s.start_time}-${s.end_time} &bull; ${s.subject_name} [${s.status}]</option>
    `).join('');
    sessionSelect.disabled = false;
    loadBtn.disabled = false;
  });

  document.getElementById('loadRosterBtn').addEventListener('click', () => {
    const sessionId = document.getElementById('markingSessionSelect').value;
    if (sessionId) loadRoster(sessionId);
  });

  // Bulk buttons
  document.getElementById('markAllPresentBtn').addEventListener('click', () => setAllStatuses('PRESENT'));
  document.getElementById('markAllAbsentBtn').addEventListener('click', () => setAllStatuses('ABSENT'));

  // Save Draft
  document.getElementById('saveDraftBtn').addEventListener('click', saveDraftAttendance);

  // Finalize
  document.getElementById('finalizeAttendanceBtn').addEventListener('click', finalizeAttendance);

  // Filter Listeners
  document.getElementById('filterSection').addEventListener('change', loadSessions);
  document.getElementById('filterStatus').addEventListener('change', loadSessions);
  document.getElementById('filterDate').addEventListener('change', loadSessions);
  document.getElementById('resetFiltersBtn').addEventListener('click', () => {
    document.getElementById('filterSection').value = '';
    document.getElementById('filterStatus').value = '';
    document.getElementById('filterDate').value = '';
    loadSessions();
  });

  // Section Reports
  document.getElementById('reportSectionSelect').addEventListener('change', loadSectionReport);
  document.getElementById('refreshReportBtn').addEventListener('click', loadSectionReport);
  document.getElementById('exportCsvBtn').addEventListener('click', exportDefaultersCsv);

  // Timetable
  document.getElementById('timetableSectionSelect').addEventListener('change', loadTimetable);

  // Student Predictor & Detection Listeners
  document.getElementById('detectUpcomingClassesBtn')?.addEventListener('click', detectUpcomingClasses);
  document.getElementById('predictorSubjectSelect')?.addEventListener('change', detectUpcomingClasses);
  document.getElementById('predictorUntilDate')?.addEventListener('change', detectUpcomingClasses);

  // Bulk Upcoming Decisions
  document.getElementById('markAllUpcomingPresent')?.addEventListener('click', () => setAllUpcomingDecisions('PRESENT'));
  document.getElementById('markAllUpcomingAbsent')?.addEventListener('click', () => setAllUpcomingDecisions('ABSENT'));
  document.getElementById('markAllUpcomingLeave')?.addEventListener('click', () => setAllUpcomingDecisions('LEAVE'));

  // Subject History Filter Chips
  document.querySelectorAll('[data-hist-filter]').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('[data-hist-filter]').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      renderSubjectHistoryTable(btn.dataset.histFilter);
    });
  });
}

// ---- ADMIN: LOAD DATA ----
async function loadAdminInit() {
  await loadSectionsDropdown('filterSection');
  await loadSectionsDropdown('markingSectionSelect');
  await loadSectionsDropdown('reportSectionSelect');
  await loadSectionsDropdown('timetableSectionSelect');
  loadSessions();
}

async function loadSectionsDropdown(selectId, triggerChange = false) {
  try {
    const res = await fetch(`${API_BASE}/sections`);
    const data = await res.json();
    const select = document.getElementById(selectId);
    if (!select) return;

    const currentVal = select.value;
    const isFilter = selectId.startsWith('filter');
    select.innerHTML = (isFilter ? '<option value="">All Sections</option>' : '') +
      data.data.map(s => `<option value="${s.id}">${s.section_name} (${s.academic_year})</option>`).join('');

    if (currentVal) select.value = currentVal;
    if (triggerChange && select.options.length > 0) {
      select.dispatchEvent(new Event('change'));
    }
  } catch (err) {
    console.error('Failed to load sections:', err);
  }
}

async function loadSessions() {
  const tbody = document.getElementById('sessionsTableBody');
  tbody.innerHTML = '<tr><td colspan="7" class="loading">Loading class sessions...</td></tr>';

  const sectionId = document.getElementById('filterSection').value;
  const status = document.getElementById('filterStatus').value;
  const date = document.getElementById('filterDate').value;

  const params = new URLSearchParams();
  if (sectionId) params.append('sectionId', sectionId);
  if (status) params.append('status', status);
  if (date) params.append('date', date);

  try {
    const res = await fetch(`${API_BASE}/sessions?${params.toString()}`);
    const data = await res.json();

    if (data.data.length === 0) {
      tbody.innerHTML = '<tr><td colspan="7" class="loading">No matching class sessions found.</td></tr>';
      return;
    }

    tbody.innerHTML = data.data.map(s => {
      const isFinal = s.status === 'FINALIZED';
      const isCancelled = s.status === 'CANCELLED';
      return `
        <tr>
          <td><span class="subject-code-badge">#${s.id}</span></td>
          <td><strong>${s.session_date}</strong><br><small class="text-muted">${s.start_time} - ${s.end_time}</small></td>
          <td>${s.section_name}</td>
          <td><strong>${s.subject_name}</strong><br><small class="text-muted">${s.subject_code}</small></td>
          <td><span class="badge badge-${s.status.toLowerCase()}">${s.status}</span></td>
          <td>${s.finalized_at ? `<small>${s.finalized_at.replace('T', ' ').substring(0, 16)}</small>` : '-'}</td>
          <td>
            <div class="header-actions">
              ${!isCancelled ? `
                <button class="btn btn-outline btn-sm" onclick="switchToMarking(${s.section_id}, ${s.id})">
                  ${isFinal ? 'Inspect / Edit' : 'Mark Attendance'}
                </button>
              ` : ''}
              ${!isFinal && !isCancelled ? `
                <button class="btn btn-outline-danger btn-sm" onclick="openCancelModal(${s.id})">Cancel</button>
              ` : ''}
            </div>
          </td>
        </tr>
      `;
    }).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="7" class="text-danger">Failed to load sessions: ${err.message}</td></tr>`;
  }
}

function switchToMarking(sectionId, sessionId) {
  // Switch tab
  document.querySelector('.tab-btn[data-tab="attendanceTab"]').click();
  const secSelect = document.getElementById('markingSectionSelect');
  secSelect.value = sectionId;
  secSelect.dispatchEvent(new Event('change'));

  setTimeout(() => {
    document.getElementById('markingSessionSelect').value = sessionId;
    loadRoster(sessionId);
  }, 300);
}

function openCancelModal(sessionId) {
  document.getElementById('cancelSessionId').value = sessionId;
  document.getElementById('cancelReasonInput').value = '';
  document.getElementById('cancelModal').classList.remove('hidden');
}

// ---- ATTENDANCE ROSTER & MARKING ----
async function loadRoster(sessionId) {
  try {
    const res = await fetch(`${API_BASE}/sessions/${sessionId}/roster`);
    const data = await res.json();
    if (!data.success) throw new Error(data.message);

    currentActiveSession = data.session;
    currentRoster = data.data;

    // Display Meta
    const secRes = await fetch(`${API_BASE}/sections`);
    const secData = await secRes.json();
    const sec = secData.data.find(s => s.id === currentActiveSession.section_id);

    const subRes = await fetch(`${API_BASE}/sections/${currentActiveSession.section_id}/subjects`);
    const subData = await subRes.json();
    const ss = subData.data.find(m => m.section_subject_id === currentActiveSession.section_subject_id);

    document.getElementById('infoSection').textContent = sec ? sec.section_name : '-';
    document.getElementById('infoSubject').textContent = ss ? `${ss.subject_name} (${ss.subject_code})` : '-';
    document.getElementById('infoDate').textContent = currentActiveSession.session_date;
    document.getElementById('infoTime').textContent = `${currentActiveSession.start_time} - ${currentActiveSession.end_time}`;
    const badge = document.getElementById('infoStatusBadge');
    badge.textContent = currentActiveSession.status;
    badge.className = `badge badge-${currentActiveSession.status.toLowerCase()}`;
    document.getElementById('rosterCountText').textContent = `${currentRoster.length} student(s) eligible on date`;

    // Render Table
    renderRosterTable();
    document.getElementById('activeSessionInfo').classList.remove('hidden');
  } catch (err) {
    showToast(err.message, 'error');
  }
}

function renderRosterTable() {
  const tbody = document.getElementById('rosterTableBody');
  const isFinalized = currentActiveSession.status === 'FINALIZED';

  tbody.innerHTML = currentRoster.map(item => `
    <tr>
      <td><span class="subject-code-badge">${item.roll_number}</span></td>
      <td><strong>${item.student_name}</strong></td>
      <td>
        ${!isFinalized ? `
          <div class="role-selector" style="grid-template-columns: 120px 120px;">
            <label class="radio-card">
              <input type="radio" name="status_${item.enrollment_id}" value="PRESENT" ${item.status === 'PRESENT' ? 'checked' : ''} onchange="updateLocalStatus(${item.enrollment_id}, 'PRESENT')">
              <span class="text-success">PRESENT</span>
            </label>
            <label class="radio-card">
              <input type="radio" name="status_${item.enrollment_id}" value="ABSENT" ${item.status === 'ABSENT' ? 'checked' : ''} onchange="updateLocalStatus(${item.enrollment_id}, 'ABSENT')">
              <span class="text-danger">ABSENT</span>
            </label>
          </div>
        ` : `
          <div class="header-actions">
            <span class="badge badge-${item.status ? item.status.toLowerCase() : 'scheduled'}">${item.status || 'UNMARKED'}</span>
            ${item.record_id ? `
              <button class="btn btn-outline btn-sm" onclick="openEditRecordModal(${item.record_id}, '${item.student_name}', '${item.status}')">
                Modify with Audit Reason
              </button>
            ` : ''}
          </div>
        `}
      </td>
    </tr>
  `).join('');

  // Disable buttons if already finalized
  document.getElementById('saveDraftBtn').disabled = isFinalized;
  document.getElementById('finalizeAttendanceBtn').disabled = isFinalized;
  document.getElementById('markAllPresentBtn').disabled = isFinalized;
  document.getElementById('markAllAbsentBtn').disabled = isFinalized;
}

function updateLocalStatus(enrollmentId, status) {
  const item = currentRoster.find(r => r.enrollment_id === enrollmentId);
  if (item) item.status = status;
}

function setAllStatuses(status) {
  currentRoster.forEach(r => r.status = status);
  renderRosterTable();
}

async function saveDraftAttendance() {
  if (!currentActiveSession) return;
  const marked = currentRoster.filter(r => r.status !== null);
  if (marked.length === 0) {
    showToast('Please mark at least one student before saving draft', 'error');
    return;
  }

  try {
    const res = await fetch(`${API_BASE}/sessions/${currentActiveSession.id}/save-draft`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        adminId: currentUser.id,
        records: marked.map(r => ({ enrollment_id: r.enrollment_id, status: r.status }))
      })
    });
    const data = await res.json();
    if (!data.success) throw new Error(data.message);

    showToast('Draft attendance saved successfully');
    loadRoster(currentActiveSession.id);
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function finalizeAttendance() {
  if (!currentActiveSession) return;

  // Pre-check for completeness
  const unmarked = currentRoster.filter(r => !r.status);
  if (unmarked.length > 0) {
    showToast(`Cannot finalize: ${unmarked.length} student(s) remain unmarked. Finalization requires a complete roster!`, 'error');
    return;
  }

  // Save current selections first
  await saveDraftAttendance();

  try {
    const res = await fetch(`${API_BASE}/sessions/${currentActiveSession.id}/finalize`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ adminId: currentUser.id })
    });
    const data = await res.json();
    if (!data.success) throw new Error(data.message);

    showToast('Session finalized! All attendance records committed to calculations.');
    loadRoster(currentActiveSession.id);
  } catch (err) {
    showToast(err.message, 'error');
  }
}

function openEditRecordModal(recordId, studentName, currentStatus) {
  document.getElementById('editRecordId').value = recordId;
  document.getElementById('editStudentName').value = studentName;
  document.getElementById('editStatusSelect').value = currentStatus || 'PRESENT';
  document.getElementById('editReasonInput').value = '';
  document.getElementById('editFinalizedModal').classList.remove('hidden');
}

// ---- REPORTS TAB ----
async function loadSectionReport() {
  const sectionId = document.getElementById('reportSectionSelect').value;
  if (!sectionId) return;

  const threshold = document.getElementById('thresholdInput').value || 75;

  try {
    // 1. Overall section report
    const res = await fetch(`${API_BASE}/reports/section/${sectionId}`);
    const data = await res.json();

    const tbody = document.getElementById('sectionReportTableBody');
    if (data.data.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" class="loading">No enrolled students in this section.</td></tr>';
      return;
    }

    document.getElementById('statStudentCount').textContent = data.data.length;
    const avg = data.data.reduce((acc, curr) => acc + curr.overall_percentage, 0) / data.data.length;
    document.getElementById('statAvgAttendance').textContent = `${avg.toFixed(1)}%`;

    tbody.innerHTML = data.data.map(s => `
      <tr>
        <td><span class="subject-code-badge">${s.roll_number}</span></td>
        <td><strong>${s.student_name}</strong></td>
        <td>${s.total_attended}</td>
        <td>${s.total_conducted}</td>
        <td><strong>${s.overall_percentage}%</strong></td>
        <td>
          <span class="badge ${s.overall_percentage >= 75 ? 'badge-present' : 'badge-absent'}">
            ${s.overall_percentage >= 75 ? 'ELIGIBLE' : 'DEFAULTER'}
          </span>
        </td>
      </tr>
    `).join('');

    // 2. Below threshold report
    const belowRes = await fetch(`${API_BASE}/reports/section/${sectionId}/below-threshold?threshold=${threshold}`);
    const belowData = await belowRes.json();

    document.getElementById('statBelowCount').textContent = belowData.data.length;
    const belowTbody = document.getElementById('belowThresholdTableBody');

    if (belowData.data.length === 0) {
      belowTbody.innerHTML = '<tr><td colspan="7" class="loading text-success">✓ Excellent! All students are above the attendance threshold.</td></tr>';
      return;
    }

    belowTbody.innerHTML = belowData.data.map(b => `
      <tr>
        <td><span class="subject-code-badge">${b.roll_number}</span></td>
        <td><strong>${b.student_name}</strong></td>
        <td>${b.subject_name} (${b.subject_code})</td>
        <td>${b.conducted}</td>
        <td>${b.attended}</td>
        <td class="text-danger">${b.missed}</td>
        <td><strong class="text-danger">${b.percentage}%</strong></td>
      </tr>
    `).join('');

  } catch (err) {
    showToast(err.message, 'error');
  }
}

function exportDefaultersCsv() {
  const table = document.getElementById('belowThresholdTableBody');
  const rows = Array.from(table.querySelectorAll('tr'));
  if (rows.length === 0 || rows[0].innerText.includes('All students are above')) {
    showToast('No defaulters to export');
    return;
  }

  let csv = 'Roll Number,Student Name,Subject,Conducted,Attended,Missed,Attendance %\n';
  rows.forEach(tr => {
    const cols = Array.from(tr.querySelectorAll('td')).map(td => `"${td.innerText.replace(/"/g, '""')}"`);
    csv += cols.join(',') + '\n';
  });

  const blob = new Blob([csv], { type: 'text/csv' });
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.setAttribute('href', url);
  a.setAttribute('download', `Attendance_Below_Threshold_Report.csv`);
  a.click();
}

// ---- TIMETABLE TAB ----
async function loadTimetable() {
  const sectionId = document.getElementById('timetableSectionSelect').value;
  if (!sectionId) return;

  const tbody = document.getElementById('timetableTableBody');
  try {
    const res = await fetch(`${API_BASE}/sections/${sectionId}/timetable`);
    const data = await res.json();

    if (data.data.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" class="loading">No timetable slots configured for this section.</td></tr>';
      return;
    }

    tbody.innerHTML = data.data.map(wt => `
      <tr>
        <td><strong>${wt.day_of_week}</strong></td>
        <td>${wt.subject_name} (${wt.subject_code})</td>
        <td>${wt.start_time}</td>
        <td>${wt.end_time}</td>
        <td><small class="text-muted">${wt.valid_from} to ${wt.valid_to}</small></td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="5" class="text-danger">${err.message}</td></tr>`;
  }
}

// ---- STUDENT PORTAL ----
let currentSubjectSessions = [];
let currentSubjectHistoryInfo = null;
let currentUpcomingSessions = [];
let currentUpcomingDecisions = {}; // { [sessionId]: 'PRESENT' | 'ABSENT' | 'LEAVE' }
let currentSubjectBaseline = { attended: 0, conducted: 0, percentage: 0 };

async function loadStudentDashboard() {
  if (!currentUser) return;
  try {
    const res = await fetch(`${API_BASE}/students/${currentUser.id}/dashboard`);
    const data = await res.json();
    if (!data.success) throw new Error(data.message);

    const d = data.data;
    document.getElementById('studentFullName').textContent = d.student_name;
    document.getElementById('studentRollBadge').textContent = d.roll_number;
    document.getElementById('studentSectionName').textContent = d.section_name;
    document.getElementById('studentYear').textContent = d.academic_year;
    document.getElementById('studentOverallPct').textContent = `${d.overall_percentage}%`;
    const secNameEl = document.getElementById('studentTimetableSectionName');
    if (secNameEl) secNameEl.textContent = d.section_name;

    const container = document.getElementById('subjectCardsContainer');
    container.innerHTML = d.subjects.map(s => {
      const subjectJson = JSON.stringify({
        section_subject_id: s.section_subject_id,
        subject_name: s.subject_name,
        subject_code: s.subject_code,
        conducted: s.conducted,
        attended: s.attended,
        missed: s.missed,
        percentage: s.percentage
      }).replace(/"/g, '&quot;');

      return `
        <div class="subject-card clickable-subject-card" onclick="openSubjectHistoryModal(${subjectJson})">
          <div class="subject-card-header">
            <div>
              <h3>${s.subject_name}</h3>
              <span class="subject-code-badge">${s.subject_code}</span>
            </div>
            <span class="badge ${s.percentage >= 75 ? 'badge-present' : 'badge-absent'}">${s.percentage}%</span>
          </div>
          <div class="progress-bar-bg">
            <div class="progress-bar-fill" style="width: ${s.percentage}%; background: ${s.percentage >= 75 ? 'var(--success)' : 'var(--danger)'}"></div>
          </div>
          <div class="session-meta" style="margin-top: 1rem; padding-bottom: 0; border: none; font-size: 0.82rem;">
            <div>Conducted: <strong>${s.conducted}</strong></div>
            <div>Attended: <strong class="text-success">${s.attended}</strong></div>
            <div>Missed: <strong class="text-danger">${s.missed}</strong></div>
          </div>
          <div class="card-action-bar mt-3">
            <button type="button" class="btn btn-sm btn-outline-primary w-full view-sessions-btn">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/><circle cx="12" cy="12" r="3"/></svg>
              View Lecture Sessions (${s.conducted}) ➔
            </button>
          </div>
        </div>
      `;
    }).join('');
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// ---- SUBJECT SESSION ATTENDANCE HISTORY MODAL ----
async function openSubjectHistoryModal(subjectInfo) {
  currentSubjectHistoryInfo = subjectInfo;
  document.getElementById('modalSubjectHistoryTitle').textContent = `${subjectInfo.subject_name} (${subjectInfo.subject_code})`;
  document.getElementById('modalSubjectHistorySubtitle').textContent = `Complete lecture history with attendance status for section ${document.getElementById('studentSectionName').textContent}`;

  document.getElementById('modalHistConducted').textContent = subjectInfo.conducted;
  document.getElementById('modalHistAttended').textContent = subjectInfo.attended;
  document.getElementById('modalHistMissed').textContent = subjectInfo.missed;
  document.getElementById('modalHistPercentage').textContent = `${subjectInfo.percentage}%`;

  // Reset active filter button
  document.querySelectorAll('[data-hist-filter]').forEach(b => b.classList.remove('active'));
  document.getElementById('filterHistAll')?.classList.add('active');

  const tbody = document.getElementById('subjectHistoryTableBody');
  tbody.innerHTML = '<tr><td colspan="6" class="loading">Loading conducted sessions...</td></tr>';
  document.getElementById('subjectHistoryModal').classList.remove('hidden');

  try {
    const res = await fetch(`${API_BASE}/students/${currentUser.id}/subject-sessions?sectionSubjectId=${subjectInfo.section_subject_id}`);
    const data = await res.json();
    if (!data.success) throw new Error(data.message);

    currentSubjectSessions = data.data;
    renderSubjectHistoryTable('ALL');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="6" class="text-danger">${err.message}</td></tr>`;
  }
}

function renderSubjectHistoryTable(filter = 'ALL') {
  const tbody = document.getElementById('subjectHistoryTableBody');
  let list = currentSubjectSessions || [];

  if (filter === 'PRESENT') {
    list = list.filter(s => s.attendance_status === 'PRESENT');
  } else if (filter === 'ABSENT') {
    list = list.filter(s => s.attendance_status === 'ABSENT');
  }

  if (list.length === 0) {
    tbody.innerHTML = `<tr><td colspan="6" class="loading">No ${filter === 'ALL' ? '' : filter.toLowerCase() + ' '}sessions found for this subject.</td></tr>`;
    return;
  }

  tbody.innerHTML = list.map((s, idx) => `
    <tr>
      <td><span class="subject-code-badge">#${list.length - idx}</span></td>
      <td><strong>${s.session_date}</strong> <span class="text-muted">(${s.day_of_week})</span></td>
      <td><code>${s.start_time} - ${s.end_time}</code></td>
      <td>
        <span class="badge ${s.attendance_status === 'PRESENT' ? 'badge-present' : 'badge-absent'}">
          ${s.attendance_status === 'PRESENT' ? '✓ PRESENT' : '✗ ABSENT'}
        </span>
      </td>
      <td><small>${s.finalized_by || 'Faculty'}</small></td>
      <td><small class="text-muted">${s.audit_reason ? '⚠️ ' + s.audit_reason : '—'}</small></td>
    </tr>
  `).join('');
}

// ---- STUDENT WEEKLY TIMETABLE ----
async function loadStudentTimetable() {
  if (!currentUser) return;
  const tbody = document.getElementById('studentTimetableTableBody');
  tbody.innerHTML = '<tr><td colspan="6" class="loading">Loading weekly timetable...</td></tr>';

  try {
    const dRes = await fetch(`${API_BASE}/students/${currentUser.id}/dashboard`);
    const dData = await dRes.json();
    if (!dData.success) throw new Error(dData.message);

    const sectionId = dData.data.section_id;
    const secNameEl = document.getElementById('studentTimetableSectionName');
    if (secNameEl) secNameEl.textContent = dData.data.section_name;

    const res = await fetch(`${API_BASE}/sections/${sectionId}/timetable`);
    const data = await res.json();
    if (!data.success) throw new Error(data.message);

    if (data.data.length === 0) {
      tbody.innerHTML = '<tr><td colspan="6" class="loading">No timetable slots found for your section.</td></tr>';
      return;
    }

    const dayOrder = { 'MONDAY': 1, 'TUESDAY': 2, 'WEDNESDAY': 3, 'THURSDAY': 4, 'FRIDAY': 5, 'SATURDAY': 6, 'SUNDAY': 7 };
    const sorted = [...data.data].sort((a, b) => {
      const dDiff = (dayOrder[a.day_of_week] || 0) - (dayOrder[b.day_of_week] || 0);
      if (dDiff !== 0) return dDiff;
      return a.start_time.localeCompare(b.start_time);
    });

    tbody.innerHTML = sorted.map(wt => `
      <tr>
        <td><strong>${wt.day_of_week}</strong></td>
        <td><span class="subject-code-badge">${wt.subject_code}</span></td>
        <td><strong>${wt.subject_name}</strong></td>
        <td><code>${wt.start_time}</code></td>
        <td><code>${wt.end_time}</code></td>
        <td><small class="text-muted">${wt.valid_from} to ${wt.valid_to}</small></td>
      </tr>
    `).join('');
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="6" class="text-danger">${err.message}</td></tr>`;
  }
}

// ---- TIMETABLE-DRIVEN ATTENDANCE PREDICTOR WITH LEAVE SUPPORT ----
function setPredictorDays(days) {
  const d = new Date();
  d.setDate(d.getDate() + Number(days));
  const dateStr = d.toISOString().split('T')[0];
  const input = document.getElementById('predictorUntilDate');
  if (input) {
    input.value = dateStr;
    detectUpcomingClasses();
  }
}

async function initPredictor() {
  if (!currentUser) return;
  try {
    const dRes = await fetch(`${API_BASE}/students/${currentUser.id}/dashboard`);
    const dData = await dRes.json();
    if (!dData.success) return;

    const select = document.getElementById('predictorSubjectSelect');
    select.innerHTML = dData.data.subjects.map(s => `
      <option value="${s.section_subject_id}">${s.subject_name} (${s.subject_code})</option>
    `).join('');

    // Default target date to 30 days ahead if not set
    const dateInput = document.getElementById('predictorUntilDate');
    if (!dateInput.value) {
      const d = new Date();
      d.setDate(d.getDate() + 30);
      dateInput.value = d.toISOString().split('T')[0];
    }

    // Automatically detect upcoming classes for default selection
    detectUpcomingClasses();
  } catch (err) {
    console.error('Failed to init predictor:', err);
  }
}

async function detectUpcomingClasses() {
  if (!currentUser) return;
  const ssId = document.getElementById('predictorSubjectSelect').value;
  const untilDate = document.getElementById('predictorUntilDate').value;
  if (!ssId) return;

  const tbody = document.getElementById('upcomingSessionsTableBody');
  tbody.innerHTML = '<tr><td colspan="5" class="loading">Detecting scheduled classes from weekly timetable...</td></tr>';
  document.getElementById('interactivePredictorContainer').classList.remove('hidden');

  try {
    // 1. Fetch current subject baseline stats from student dashboard
    const dashRes = await fetch(`${API_BASE}/students/${currentUser.id}/dashboard`);
    const dashData = await dashRes.json();
    const currSubj = dashData.data.subjects.find(s => s.section_subject_id === Number(ssId));
    if (currSubj) {
      currentSubjectBaseline = {
        attended: currSubj.attended,
        conducted: currSubj.conducted,
        percentage: currSubj.percentage
      };
    }

    // 2. Fetch upcoming scheduled sessions up to untilDate
    const res = await fetch(`${API_BASE}/students/${currentUser.id}/upcoming-sessions?sectionSubjectId=${ssId}&untilDate=${untilDate}`);
    const data = await res.json();
    if (!data.success) throw new Error(data.message);

    currentUpcomingSessions = data.data;

    // Initialize all detected sessions as PRESENT by default
    currentUpcomingDecisions = {};
    currentUpcomingSessions.forEach(s => {
      currentUpcomingDecisions[s.session_id] = 'PRESENT';
    });

    renderUpcomingSessionsTable();
    updateInteractiveCalculations();
  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="5" class="text-danger">${err.message}</td></tr>`;
    showToast(err.message, 'error');
  }
}

function setSessionDecision(sessionId, decision) {
  currentUpcomingDecisions[sessionId] = decision;
  renderUpcomingSessionsTable();
  updateInteractiveCalculations();
}

function setAllUpcomingDecisions(decision) {
  currentUpcomingSessions.forEach(s => {
    currentUpcomingDecisions[s.session_id] = decision;
  });
  renderUpcomingSessionsTable();
  updateInteractiveCalculations();
}

function renderUpcomingSessionsTable() {
  const tbody = document.getElementById('upcomingSessionsTableBody');
  if (!currentUpcomingSessions || currentUpcomingSessions.length === 0) {
    tbody.innerHTML = `
      <tr>
        <td colspan="5" class="loading">
          No scheduled classes detected between today and the target date. Check if classes have concluded or select a further date horizon.
        </td>
      </tr>`;
    return;
  }

  tbody.innerHTML = currentUpcomingSessions.map((s, idx) => {
    const curDecision = currentUpcomingDecisions[s.session_id] || 'PRESENT';
    return `
      <tr class="upcoming-session-row">
        <td><span class="subject-code-badge">#${idx + 1}</span></td>
        <td><strong>${s.session_date}</strong> <span class="text-muted">(${s.day_of_week})</span></td>
        <td><code>${s.start_time} - ${s.end_time}</code></td>
        <td>${s.subject_name} (${s.subject_code})</td>
        <td>
          <div class="decision-toggle-group">
            <button type="button" 
              class="decision-btn btn-present ${curDecision === 'PRESENT' ? 'active' : ''}" 
              onclick="setSessionDecision(${s.session_id}, 'PRESENT')">
              ✓ PRESENT
            </button>
            <button type="button" 
              class="decision-btn btn-absent ${curDecision === 'ABSENT' ? 'active' : ''}" 
              onclick="setSessionDecision(${s.session_id}, 'ABSENT')">
              ✗ ABSENT
            </button>
            <button type="button" 
              class="decision-btn btn-leave ${curDecision === 'LEAVE' ? 'active' : ''}" 
              onclick="setSessionDecision(${s.session_id}, 'LEAVE')">
              🏖️ LEAVE
            </button>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

function updateInteractiveCalculations() {
  const T_curr = currentSubjectBaseline.conducted;
  const P_curr = currentSubjectBaseline.attended;

  let plannedPresent = 0;
  let plannedAbsent = 0;
  let plannedLeave = 0;

  for (const s of currentUpcomingSessions) {
    const dec = currentUpcomingDecisions[s.session_id] || 'PRESENT';
    if (dec === 'PRESENT') plannedPresent++;
    else if (dec === 'ABSENT') plannedAbsent++;
    else if (dec === 'LEAVE') plannedLeave++;
  }

  // LEAVE is excused: excluded from conducted denominator!
  const projectedConducted = T_curr + plannedPresent + plannedAbsent;
  const projectedAttended = P_curr + plannedPresent;
  const projectedPct = projectedConducted > 0
    ? Number(((projectedAttended / projectedConducted) * 100).toFixed(2))
    : 0;

  // Update UI Elements
  document.getElementById('predCurrentAttended').textContent = P_curr;
  document.getElementById('predCurrentConducted').textContent = T_curr;
  document.getElementById('predCurrentPct').textContent = `${currentSubjectBaseline.percentage}%`;

  document.getElementById('predPlannedPresCount').textContent = `${plannedPresent} Pres`;
  document.getElementById('predPlannedAbsCount').textContent = `${plannedAbsent} Abs`;
  document.getElementById('predPlannedLeaveCount').textContent = `${plannedLeave} Leave`;
  document.getElementById('predUpcomingCount').textContent = currentUpcomingSessions.length;

  document.getElementById('predProjectedScore').textContent = `${projectedAttended} / ${projectedConducted}`;
  const pctEl = document.getElementById('predProjectedPct');
  pctEl.textContent = `${projectedPct}%`;

  const pill = document.getElementById('predEligibilityPill');
  const bufferEl = document.getElementById('predBufferExplanation');
  const statusCard = document.getElementById('predStatusCard');

  if (projectedPct >= 75.0) {
    pill.textContent = 'ELIGIBLE (≥ 75%)';
    pill.className = 'stat-value text-success font-bold';
    pctEl.style.color = 'var(--success)';
    if (statusCard) statusCard.style.borderColor = 'var(--success)';
    // Safe miss buffer: (P) / (T + m) >= 0.75 => m <= (P - 0.75 * T) / 0.75
    const margin = Math.max(0, Math.floor((projectedAttended - 0.75 * projectedConducted) / 0.75));
    bufferEl.innerHTML = `<span class="text-success">✓ Safe buffer:</span> You can safely miss up to <strong>${margin}</strong> more lecture(s) without falling below 75%.`;
  } else {
    pill.textContent = 'DEFAULTER (< 75%)';
    pill.className = 'stat-value text-danger font-bold';
    pctEl.style.color = 'var(--danger)';
    if (statusCard) statusCard.style.borderColor = 'var(--danger)';
    // Classes needed: (P + c) / (T + c) >= 0.75 => c >= 3 * T - 4 * P
    const needed = Math.max(0, Math.ceil(3 * projectedConducted - 4 * projectedAttended));
    bufferEl.innerHTML = `<span class="text-danger">⚠️ Attendance shortage:</span> You must attend at least <strong>${needed}</strong> more consecutive lecture(s) to reach 75%.`;
  }
}
