toastr.options = {
    closeButton: true,
    debug: false,
    newestOnTop: true,
    progressBar: true,
    positionClass: "toast-top-right",
    preventDuplicates: false,
    onclick: null,
    showDuration: "300",
    hideDuration: "1000",
    timeOut: "5000",
    extendedTimeOut: "1000",
    showEasing: "swing",
    hideEasing: "linear",
    showMethod: "fadeIn",
    hideMethod: "fadeOut"
};

function parseMsg(msg) {
    const [pureMsg, ttl] = msg.split(";ttl=");

    const currentJsUnixTimestamp = new Date().getTime();

    if (ttl && parseInt(ttl) + 5000 < currentJsUnixTimestamp) {
        return [pureMsg, false];
    }

    return [pureMsg, true];
}

function toastNotice(msg) {
    const [pureMsg, needToShow] = parseMsg(msg);

    if (needToShow) {
        toastr["success"](pureMsg, "알림");
    }
}

function toastWarning(msg) {
    const [pureMsg, needToShow] = parseMsg(msg);

    if (needToShow) {
        toastr["warning"](pureMsg, "경고");
    }
}


function showDeleteConfirmDialog(msg, callback) {
    Swal.fire({
        title: '주의',
        text: msg,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#2e3bc0',
        cancelButtonColor: '#d33',
        confirmButtonText: '승인',
        cancelButtonText: '취소',
        reverseButtons: true, // 버튼 순서 거꾸로
    }).then((result) => {
        if (result.isConfirmed) {
            callback();
        }
    });
}

function showDeleteConfirmDialogMsg(msg, element) {
    Swal.fire({
        title: '주의',
        text: msg,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#2e3bc0',
        cancelButtonColor: '#d33',
        confirmButtonText: '삭제',
        cancelButtonText: '취소',
        reverseButtons: true,
    }).then((result) => {
        // 사용자가 '삭제' 버튼을 누른 경우, 폼을 제출합니다.
        if (result.isConfirmed) {
            $(element).next().submit();
        }
    });
}

function showDeleteNotificationConfirmDialogMsg(msg) {
    return new Promise((resolve) => {
        Swal.fire({
            title: '주의',
            text: msg,
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#2e3bc0',
            cancelButtonColor: '#d33',
            confirmButtonText: '삭제',
            cancelButtonText: '취소',
            reverseButtons: true,
        }).then((result) => {
            // 사용자가 '삭제' 버튼을 누른 경우, true 를 반환
            resolve(result.isConfirmed);
        });
    });
}

function showModifyConfirmDialogMsg(msg, element) {
    Swal.fire({
        title: '주의',
        text: msg,
        icon: 'warning',
        showCancelButton: true,
        confirmButtonColor: '#2e3bc0',
        cancelButtonColor: '#d33',
        confirmButtonText: '수정',
        cancelButtonText: '취소',
        reverseButtons: true,
    }).then((result) => {
        // 사용자가 '수정' 버튼을 누른 경우, 폼을 제출합니다.
        if (result.isConfirmed) {
            $(element).next().submit();
        }
    });
}

function showMovetoMainConfirmDialog(msg, callback) {
    Swal.fire({
        title: '성공',
        text: msg,
        icon: 'success',
        showCancelButton: true,
        confirmButtonColor: '#2e3bc0',
        cancelButtonColor: '#6dd2ce',
        confirmButtonText: '맛지도로 이동',
        cancelButtonText: '계속 등록',
        reverseButtons: true, // 버튼 순서 거꾸로
    }).then((result) => {
        if (result.isConfirmed) {
            callback();
        }
    });
}
