// O menu é reutilizado nas duas páginas da aplicação.
const menuButton = document.querySelector('#menuButton');
const mainNav = document.querySelector('#mainNav');

menuButton?.addEventListener('click', () => {
    const isOpen = mainNav.classList.toggle('open');
    menuButton.setAttribute('aria-expanded', String(isOpen));
});

// Na tela de documentos, a busca filtra as linhas das tabelas.
const searchInput = document.querySelector('#searchInput');

searchInput?.addEventListener('input', () => {
    const term = searchInput.value.toLowerCase().trim();

    document.querySelectorAll('tbody tr').forEach((row) => {
        row.hidden = !row.textContent.toLowerCase().includes(term);
    });
});
