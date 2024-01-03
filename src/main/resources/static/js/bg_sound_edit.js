let bgSoundIndex = 0;
const bgSoundCont = document.querySelector(".bg-sound__cont");
const bgSoundOptionBox = document.querySelector(
    ".bg-sound__cont .bg-sound__option"
);
function addBgSound() {
    if (!confirm("배경음악을 추가하시겠습니까?")) return;
    if (bgSoundIndex >= 4) {
        alert("배경음악은 작품당 최대 4개까지 등록 가능합니다.");
        return;
    }
    bgSoundIndex++;
    const div = document.createElement("div");
    div.classList.add("bg-sound__box", "flex");
    div.dataset.soundIndex = `${bgSoundIndex}`;
    div.innerHTML = `
        <div class="bg-sound__element">
            <audio src="/audio/Aces_High_Funkorama.mp3" controls></audio>
        </div>
        <div class="scene-start__num bg-sound__element">
            <span>시작 장면</span>
            <input type="text" value="1" oninput="this.value = this.value.replace(/[^0-9]/g, '')" maxlength="2" disabled>
        </div>
        <div class="scene-end__num bg-sound__element">
            <span>끝 장면</span>
            <input type="text" value="2" oninput="this.value = this.value.replace(/[^0-9]/g, '')" maxlength="3" disabled>
        </div>
        <div class="bg-sound__option align-items-center flex grow" style="justify-content: right;">
            <div class="bg-sound__element">
                <button class="bg-sound__edit-button edit-button" onclick="editBgSound(${bgSoundIndex})">편집</button>
            </div>
            <div class="bg-sound__element">
                <button class="edit-button" onclick="deleteBgSound(${bgSoundIndex})">지우기</button>
            </div>
        </div>
    `;
    bgSoundCont.appendChild(div);
}
function deleteBgSound(index) {
    if (
        !confirm(
            "해당 배경음악을 삭제하시겠습니까? \n(삭제 시 복구가 불가합니다.)"
        )
    )
        return;
    const bgSound = document.querySelector(`div[data-sound-index="${index}"]`);
    bgSound.remove();
    bgSoundIndex--;
}
function editBgSound(index) {
    const bgSoundEditButton = document.querySelector(
        `div[data-sound-index="${index}"] .bg-sound__edit-button`
    );
    const startNumInput = document.querySelector(
        `div[data-sound-index="${index}"] .scene-start__num input`
    );
    const endNumInput = document.querySelector(
        `div[data-sound-index="${index}"] .scene-end__num input`
    );
    if (bgSoundEditButton.classList.contains("editing")) {
        const startNum = parseInt(startNumInput.value);
        const endNum = parseInt(endNumInput.value);
        if (startNum < 1) {
            startNumInput.value = "1";
        }
        if (endNum < 2) {
            endNumInput.value = "2";
        }
        if (endNum - startNum < 1) {
            alert("배경음악은 최소 두 장면 이상 이어져야 합니다.");
            endNumInput.value = `${startNum + 1}`;
            return;
        }
        startNumInput.disabled = true;
        endNumInput.disabled = true;
        bgSoundEditButton.classList.toggle("editing");
    } else {
        startNumInput.disabled = false;
        endNumInput.disabled = false;
        bgSoundEditButton.classList.toggle("editing");
    }
}
