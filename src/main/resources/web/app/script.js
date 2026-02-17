document.addEventListener('DOMContentLoaded', function () {
    // Check if already answered
    var form = document.getElementById('survey-form');
    var alreadyAnswered = document.getElementById('already-answered');
    if (form && alreadyAnswered && localStorage.getItem('sqq_survey_done')) {
        form.classList.add('hidden');
        alreadyAnswered.classList.remove('hidden');
        return;
    }

    // Validate and mark as answered on form submit
    if (form) {
        form.addEventListener('submit', function (e) {
            var checked = form.querySelectorAll('input[type="checkbox"]:checked');
            if (checked.length === 0) {
                e.preventDefault();
                var msg = document.getElementById('q1-error');
                if (msg) msg.classList.remove('hidden');
                return;
            }
            localStorage.setItem('sqq_survey_done', '1');
        });
    }

    // Prevent duplicate selections across priority selects
    var selects = document.querySelectorAll('.priority-select');
    if (selects.length === 0) return;

    selects.forEach(function (select) {
        select.addEventListener('change', updateOptions);
    });

    function updateOptions() {
        var selected = [];
        selects.forEach(function (s) {
            if (s.value) selected.push(s.value);
        });

        selects.forEach(function (s) {
            var options = s.querySelectorAll('option');
            options.forEach(function (opt) {
                if (!opt.value) return;
                opt.disabled = selected.includes(opt.value) && s.value !== opt.value;
            });
        });
    }
});
