document.addEventListener('DOMContentLoaded', function () {
    var form = document.getElementById('survey-form');
    var alreadyAnswered = document.getElementById('already-answered');

    // Si déjà répondu, on masque le formulaire.
    if (form && alreadyAnswered && localStorage.getItem('sqq_vacances_2026_done')) {
        form.classList.add('hidden');
        alreadyAnswered.classList.remove('hidden');
        return;
    }

    // Marque comme répondu à l'envoi (ne rien cocher est une réponse valide).
    if (form) {
        form.addEventListener('submit', function () {
            localStorage.setItem('sqq_vacances_2026_done', '1');
        });
    }
});
