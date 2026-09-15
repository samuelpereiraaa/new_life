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

// Na home, mostramos os arquivos escolhidos no cartão de upload.
const fileInput = document.querySelector('#fileInput');
const fileList = document.querySelector('#fileList');
const dropZone = document.querySelector('#dropZone');

function renderFiles(files) {
    if (!fileList) return;

    fileList.innerHTML = '';
    [...files].slice(0, 4).forEach((file) => {
        const item = document.createElement('div');
        item.className = 'file-item';
        item.innerHTML = `<span>▤ ${file.name}</span><small>Pronto para enviar</small>`;
        fileList.append(item);
    });
}

fileInput?.addEventListener('change', (event) => renderFiles(event.target.files));

['dragenter', 'dragover'].forEach((eventName) => {
    dropZone?.addEventListener(eventName, (event) => {
        event.preventDefault();
        dropZone.classList.add('dragging');
    });
});

['dragleave', 'drop'].forEach((eventName) => {
    dropZone?.addEventListener(eventName, (event) => {
        event.preventDefault();
        dropZone.classList.remove('dragging');
    });
});

dropZone?.addEventListener('drop', (event) => renderFiles(event.dataTransfer.files));
