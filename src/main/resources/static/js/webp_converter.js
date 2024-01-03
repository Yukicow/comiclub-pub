async function createWebpImg(file) {
    return new Promise((resolve) => {
        const reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = function () {
            const img = new Image();
            img.src = reader.result;
            img.onload = async function () {
                const canvas = document.createElement("canvas");
                const context = canvas.getContext("2d");
                canvas.width = img.width;
                canvas.height = img.height;
                context.drawImage(img, 0, 0, img.width, img.height);
                canvas.toBlob(
                    (blob) => {
                        resolve(blob);
                    },
                    "image/webp",
                    100
                );
            };
        };
    });
}
