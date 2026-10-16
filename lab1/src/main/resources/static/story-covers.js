(() => {
    const escapeXml = value => String(value ?? '').replace(/[&<>"']/g, char => ({
        '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&apos;'
    }[char]));

    window.storyCoverUrl = (value, title = 'Truyện Mộc') => {
        if (value) {
            try {
                const url = new URL(String(value), location.origin);
                if (url.protocol === 'http:' || url.protocol === 'https:') return url.href;
            } catch {}
        }

        let hash = 0;
        for (const char of String(title)) hash = (hash * 31 + char.codePointAt(0)) | 0;
        const hue = ((hash % 360) + 360) % 360;
        const lines = String(title || 'Truyện Mộc').trim().split(/\s+/).reduce((rows, word) => {
            const last = rows[rows.length - 1];
            if (last && `${last} ${word}`.length <= 18) rows[rows.length - 1] += ` ${word}`;
            else if (rows.length < 3) rows.push(word);
            return rows;
        }, []);
        const titleLines = lines.map((line, index) =>
            `<text x="50%" y="${405 + index * 52}" text-anchor="middle">${escapeXml(line)}</text>`
        ).join('');
        const svg = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 480 680"><defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1"><stop stop-color="hsl(${hue} 58% 76%)"/><stop offset="1" stop-color="hsl(${(hue + 55) % 360} 55% 42%)"/></linearGradient></defs><rect width="480" height="680" rx="28" fill="url(#g)"/><circle cx="385" cy="115" r="110" fill="#fff" opacity=".12"/><circle cx="80" cy="540" r="150" fill="#fff" opacity=".1"/><path d="M0 530Q150 420 260 535T480 500V680H0Z" fill="#122849" opacity=".2"/><text x="50%" y="325" text-anchor="middle" font-family="serif" font-size="64" font-weight="700" fill="#fff">Mộc</text><g font-family="sans-serif" font-size="29" font-weight="700" fill="#fff">${titleLines}</g><text x="50%" y="615" text-anchor="middle" font-family="sans-serif" font-size="18" letter-spacing="5" fill="#fff" opacity=".85">TRUYỆN MỘC</text></svg>`;
        return `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(svg)}`;
    };
})();
