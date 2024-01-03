let currentIndex = 1;
let isSliding = false;
const slideBox = document.querySelector(".slide-box");
const prevSlideButton = document.getElementById("prev-slide-button");
const nextSlideButton = document.getElementById("next-slide-button");
const slides = document.querySelectorAll(".slide");
const lastIndex = slides.length - 1;
prevSlideButton.addEventListener("click", () => {
    if (isSliding)
        return;
    currentIndex = currentIndex <= 0 ? -1 : currentIndex - 2; // 2를 빼야 moveSlide()가 실행될 때 1이 더해져 최종적으로 -1아 됨
    clearInterval(slideInterval);
    slideInterval = getInterval();
});
nextSlideButton.addEventListener("click", () => {
    if (isSliding)
        return;
    currentIndex = currentIndex >= lastIndex ? lastIndex - 1 : currentIndex; // 아무것도 더하지 않아야 moveSlide()가 실행될 때 1이 더해져 최종적으로 +1이 됨
    clearInterval(slideInterval);
    slideInterval = getInterval();
});
const getInterval = () => {
    moveSlide();
    return setInterval(moveSlide, 4000);
};
function moveSlide() {
    isSliding = true;
    setTimeout(() => { isSliding = false; }, 500);
    currentIndex++;
    slideBox.style.left = `-${100 * currentIndex}%`;
    if (currentIndex <= 0) {
        setTimeout(() => {
            slideBox.style.transitionDuration = "0ms";
            slideBox.style.left = `-${100 * (lastIndex - 1)}%`;
            setTimeout(() => slideBox.style.transitionDuration = "400ms", 50);
            currentIndex = lastIndex - 1;
        }, 400);
    }
    else if (currentIndex >= lastIndex) {
        setTimeout(() => {
            slideBox.style.transitionDuration = "0ms";
            slideBox.style.left = `-100%`;
            setTimeout(() => slideBox.style.transitionDuration = "400ms", 50);
            currentIndex = 1;
        }, 400);
    }
}
let slideInterval = getInterval(); // interval 등록
