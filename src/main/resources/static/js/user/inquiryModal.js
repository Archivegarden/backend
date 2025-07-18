$(document).ready(function () {

    let productId = $('#product').data('id');

    loadInquiries(productId, 1);

    if ($('.inquiry_list #noDataMessage').length > 0) {
        $('.footer').addClass('fixed');
    }

    $(document).on("click", ".inquiry_items", function () {
        let inquiryItems = $(this);
        let currentContent = inquiryItems.next(".inquiry_content");
        let isSecret = inquiryItems.find('img[src*="lock.svg"]').length > 0;
        let isWriter = inquiryItems.hasClass('isWriter');

        if (currentContent.length > 0) {
            if (isSecret) {
                if (isWriter) {
                    toggleContent(currentContent);
                } else {
                }
            } else {
                toggleContent(currentContent);
            }
        } else {
        }
    });

    // 수정 버튼
    $(document).on("click", "#inquiryTable .edit_btn", function () {
        editInquiryModal(this);
    });

    $('#inquiryModal .close_btn').on('click', function () {
        closeInquiryModal();
    });

    // 삭제 버튼
    $(document).on("click", ".delete_btn", function () {
        let inquiryItem = $(this).closest(".inquiry_content").prev(".inquiry_items");
        let inquiryContent = inquiryItem.next(".inquiry_content");
        let inquiryId = $(this).data("id");

        deleteProductInquiry(productId, inquiryId, inquiryItem, inquiryContent);
    });

    // 모달 닫기 버튼
    $('.close_btn').on('click', function () {
        $('#inquiryModal').hide();

        // 모달이 닫히면 inquiry_content 원래 상태로 복원
        let inquiryContent = $('#inquiryModal').closest('.inquiry_content');
        inquiryContent.find('.inquiry_cont').css({
            visibility: "visible",
            position: "relative"
        });

        // 첫 번째 tr의 border-top을 원래대로 되돌리기
        $('#product .inquiry_wrap.list .inquiry_table tbody tr:first-child').css({
            'border-top': 'none'
        });
    });

    $(document).on('click', '.page', function () {
        let currentPage = parseInt($(this).data('page')); // 숫자 타입으로 변환
        $('.page').removeClass('active');  // 기존의 .active 클래스 제거
        $(this).addClass('active');  // 클릭된 페이지에 .active 클래스 추가
        loadInquiries(productId, currentPage);
    });

    $(document).on('click', '.prev, .next', function () {
        let currentPage = $(this).data('page');
        loadInquiries(productId, currentPage);
    });

    let csrfToken = $("meta[name='_csrf']").attr("content");
    let csrfHeader = $("meta[name='_csrf_header']").attr("content");

    $('#inquiryBtn').on('click', function () {
        // 수정 모달 닫기
        $('#editInquiryModal').remove();

        // 숨겨져 있던 inquiry_cont 다시 보이게
        $('.inquiry_cont').css({
            visibility: 'visible',
            position: 'relative'
        });

        $.ajax({
            url: '/ajax/login/status',
            type: 'GET',
            beforeSend: function (xhr) {
                xhr.setRequestHeader(csrfHeader, csrfToken)
            },
            success: function (result) {
                if (result.status == 200) {
                    openInquiryModal();

                    // 첫 번째 tr에 border-top 스타일 변경
                    $('#product .inquiry_wrap.list .inquiry_table tbody tr:first-child').css({
                        'border-top': '1px solid #d7d7d7'
                    });
                }
            },
            error: function (xhr) {
                if (xhr.status == 401) {
                    Swal.fire({
                        text: '로그인이 필요한 기능입니다.',
                        showCancelButton: true,
                        cancelButtonText: '취소',
                        confirmButtonText: '로그인',
                        customClass: mySwalConfirm,
                        reverseButtons: true,
                        buttonsStyling: false,
                    }).then((result) => {
                        if (result.isConfirmed) {
                            window.location.href = '/login';
                        }
                    });
                } else {
                    Swal.fire({
                        text: "로그인 상태를 확인하는 중 오류가 발생했습니다.",
                        showConfirmButton: true,
                        confirmButtonText: '확인',
                        customClass: mySwal,
                        buttonsStyling: false
                    });
                }
            }
        });
    });

    $('#inquiryModal .submit_btn').on('click', function () {
        let title = $('#inquiryModal #title').val().trim();
        let content = $('#inquiryModal #content').val().trim();
        let isSecret = $('#inquiryModal #secret').prop('checked');

        if (validateBeforeInquiryModalSubmit()) {
            let requestUrl = '/ajax/inquiries/add';

            let requestData = {
                'productId': productId,
                'title': title,
                'content': content,
                'isSecret': isSecret
            };

            $.ajax({
                url: requestUrl,
                type: 'POST',
                data: requestData,
                beforeSend: function (xhr) {
                    xhr.setRequestHeader(csrfHeader, csrfToken)
                },
                success: function () {
                    loadInquiries(productId, 1);
                    closeInquiryModal();
                },
                error: function (error) {
                    Swal.fire({
                        text: error.message,
                        showConfirmButton: true,
                        confirmButtonText: '확인',
                        customClass: mySwal,
                        buttonsStyling: false
                    });
                }
            });
        }
    });

});

function loadInquiries(productId, currentPage) {
    $.ajax({
        type: 'GET',
        url: '/ajax/inquiries/' + productId + '?page=' + currentPage,
        success: function (data) {
            renderPagination(data.totalElements, currentPage);
            let totalCount = $('.totalCount');
            totalCount.text(data.totalElements);
            let inquiryTable = $('#inquiryTable');
            let tbody = inquiryTable.find('tbody');

            if (data && data.content.length > 0) {
                tbody.empty();
                data.content.forEach(function (item) {
                    let inquiryTitle = item.isSecret && !item.isWriter ? '비밀글입니다.' : item.title;
                    let answeredClass = item.isAnswered === '답변완료' ? 'complete' : '';
                    let inquiryItem = `
                        <tr class="inquiry_items ${item.isWriter ? 'isWriter' : ''}">
                            <td class="title_td">
                                <div class="title">
                                    ${item.isSecret ? '<img src="../../images/lock.svg">' : ''}
                                    <span class="title">${inquiryTitle}</span>
                                </div>
                            </td>
                            <td><span class="writer">${item.writerLoginId}</span></td>
                            <td><span class="date">${item.createdAt}</span></td>
                            <td><span class="answer ${answeredClass}">${item.isAnswered}</span></td>
                        </tr>
                        ${item.isSecret && !item.isWriter ? '' :
                        `<tr class="inquiry_content">
                            <td colspan="4">
                                <div class="inquiry_cont" id="content">
                                    <div class="inquiry_question">
                                        <div class="inquiry_text">
                                            <span>Q</span>
                                            <div class="contents">
                                            <span class="question">${item.title}</span>
                                            <span class="content">${item.content}</span>
                                            </div>
                                        </div>
                                        <div class="menu">
                                            ${item.isWriter ?
                                            `<button class="menu_toggle">
                                            <img src="../../../images/more_horiz.svg">
                                            </button>
                                                <div class="dropdown_menu">
                                                    <ul>
                                                        <li>
                                                            <button type="button" class="edit_btn" data-id="${item.id}">수정</button>
                                                        </li>
                                                        <li>
                                                            <button type="button" class="delete_btn" data-id="${item.id}">삭제</button>
                                                        </li>
                                                    </ul>
                                                </div>` : ''
                        }
                                        </div>
                                    </div>
                                    <div class="inquiry_answer">
                                        <span>A</span>
                                        <span class="answer">${item.answer || '답변이 작성되지 않았습니다.'}</span>
                                    </div>
                                </div>
                            </td>
                        </tr>
                    `}`;
                    tbody.append(inquiryItem);
                });
            } else {
                tbody.empty();
                let noDataMessage = `
                    <tr id="noDataMessage">
                        <td colspan="4">등록된 상품문의가 없습니다.</td>
                    </tr>
                `;
                tbody.append(noDataMessage);
            }
        },
        error: function () {
            Swal.fire({
                text: "상품 문의를 불러오는 데 실패했습니다.",
                showConfirmButton: true,
                confirmButtonText: '확인',
                customClass: mySwal,
                buttonsStyling: false
            });
        }
    });
}

// 페이지네이션 렌더링
function renderPagination(totalElements, currentPage) {
    const paginationSize = 5; // 한 번에 보여줄 페이지 수
    const totalPages = Math.ceil(totalElements / 10);

    if (totalPages === 0) {
        $('#pagination').empty();
        return;
    }

    const pagination = $('#pagination');
    pagination.empty();

    let currentGroup = Math.floor((currentPage - 1) / paginationSize);
    let startPage = currentGroup * paginationSize + 1;
    let endPage = Math.min(startPage + paginationSize - 1, totalPages);

    // 이전 페이지
    if (currentPage > 1) {
        let prevPage = currentPage - 1;
        pagination.append(`
            <li><a class="prev" data-page="${prevPage}">
                <img src="../../images/keyboard_arrow_left.svg" alt="이전 페이지">
            </a></li>
        `);
    } else {
        pagination.append(`
            <li><a class="prev disabled">
                <img src="../../images/keyboard_arrow_left.svg" alt="이전 페이지">
            </a></li>
        `);
    }

    // 페이지 번호
    for (let i = startPage; i <= endPage; i++) {
        let isActive = currentPage === i ? 'active' : '';
        pagination.append(`
            <li><a class="page ${isActive}" data-page="${i}">${i}</a></li>
        `);
    }

    // 다음 페이지
    if (currentPage < totalPages) {
        let nextPage = currentPage + 1;
        pagination.append(`
            <li><a class="next" data-page="${nextPage}">
                <img src="../../images/keyboard_arrow_right.svg" alt="다음 페이지">
            </a></li>
        `);
    } else {
        pagination.append(`
            <li><a class="next disabled">
                <img src="../../images/keyboard_arrow_right.svg" alt="다음 페이지">
            </a></li>
        `);
    }
}

// 상품문의 폼 열기
function openInquiryModal() {
    let inquiryModal = $("#inquiryModal");
    inquiryModal.removeData('id');

    inquiryModal.css("display", "flex");
    $('#inquiryModal .submit_btn').prop('disabled', false);
    $("#inquiryModal .submit_btn").text("등록");
}

function closeInquiryModal() {
    $('#inquiryModal').hide();
    $('#inquiryModal').removeData('id');
    $('#inquiryModal #title').val('');
    $('#inquiryModal #content').val('');
    $('#inquiryModal #secret').prop('checked', false);
    $("#inquiryModal .submit_btn").text("등록");

    // inquiry_content 원래 상태로 복원
    let inquiryContent = $('#inquiryModal').closest('.inquiry_content');
    inquiryContent.find('.inquiry_cont').css({
        visibility: "visible",
        position: "relative"
    });

    $('#product .inquiry_wrap.list .inquiry_table tbody tr:first-child').css({
        'border-top': 'none'
    });
}

// 비밀글 찾기
function findSecretItem(editButton) {
    let inquiryItems = $(editButton).closest('.inquiry_content').prev('.inquiry_items');
    let secretItem = inquiryItems.find('img[src*="lock.svg"]');
    return secretItem.length > 0;
}

// 상품문의 수정 폼
function editInquiryModal(editButton) {
    if ($('#inquiryModal').is(':visible')) {
        closeInquiryModal();
    }
    if ($(editButton).closest('.inquiry_cont').parent().find('#editInquiryModal').length > 0) {
        return;
    }

    let originalModal = $('#inquiryModal');
    let clonedModal = originalModal.clone().attr('id', 'editInquiryModal');
    let inquiryId = $(editButton).data('id');

    // 질문 데이터 가져오기
    let inquiryTitle = $(editButton).closest('.inquiry_cont').find('.question').text().trim();
    let inquiryContent = $(editButton).closest('.inquiry_cont').find('.content').text().trim();
    let isSecret = findSecretItem(editButton);

    clonedModal.data('id', inquiryId);
    clonedModal.find('#title').val(inquiryTitle);
    clonedModal.find('#content').val(inquiryContent);
    clonedModal.find('#secret').prop('checked', isSecret);
    clonedModal.find('.submit_btn').text('완료');


    // 수정용 모달 제출 버튼 이벤트 추가
    clonedModal.find('.submit_btn').on('click', function () {
        let title = clonedModal.find('#title').val().trim();
        let content = clonedModal.find('#content').val().trim();
        let isSecret = clonedModal.find('#secret').prop('checked');

        if (title === '') {
            Swal.fire({
                text: "제목을 작성해 주세요.",
                showConfirmButton: true,
                confirmButtonText: '확인',
                customClass: mySwal,
                buttonsStyling: false
            });
            return false;
        }

        if (content === '') {
            Swal.fire({
                text: "내용을 작성해 주세요.",
                showConfirmButton: true,
                confirmButtonText: '확인',
                customClass: mySwal,
                buttonsStyling: false
            });
            return false;
        }

        $(this).prop('disabled', true);

        $.ajax({
            url: '/ajax/inquiries/' + inquiryId + '/edit',
            type: 'POST',
            data: {
                productId: $('#product').data('id'),
                title: title,
                content: content,
                isSecret: isSecret
            },
            beforeSend: function (xhr) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            },
            success: function () {
                loadInquiries($('#product').data('id'), 1);
                clonedModal.remove(); // 모달 제거
            },
            error: function (err) {
                Swal.fire({
                    text: err.message || "오류가 발생했습니다.",
                    confirmButtonText: '확인',
                    customClass: mySwal,
                    buttonsStyling: false
                });
                clonedModal.find('.submit_btn').prop('disabled', false);
            }
        });
    });

    clonedModal.find('.close_btn').on('click', function () {
        clonedModal.remove();

        // 숨겼던 원래 질문 내용 다시 보여주기
        let inquiryCont = $(editButton).closest('.inquiry_cont');
        inquiryCont.css({
            visibility: "visible",
            position: "relative"
        });
    });

    // 수정 중인 질문 아래에 붙이기
    let inquiryCont = $(editButton).closest('.inquiry_cont').parent();
    inquiryCont.append(clonedModal);

    // 기존 질문 숨기기
    inquiryCont.children().not(clonedModal).css({
        visibility: "hidden",
        position: "absolute"
    });

    // 스타일 적용 및 표시
    clonedModal.css({
        display: 'flex',
        position: 'relative',
        width: '100%',
        zIndex: 10,
        top: 0,
        left: 0
    });
}

// 상품문의 클릭 시 내용 보이게
function toggleContent(content) {
    if ($('#editInquiryModal').length > 0) {
        let editModal = $('#editInquiryModal');
        let parentContent = editModal.closest('.inquiry_content');

        parentContent.find('.inquiry_cont').css({
            visibility: 'visible',
            position: 'relative'
        });

        editModal.remove();
    }

    if (content.is(":visible")) {
        content.find("#inquiryModal").hide();
        content.children().css({
            visibility: "visible",
            position: "relative"
        });

        content.stop(true, true).slideUp("fast", function () {
            content.css("border-bottom", "");
            content.prev(".inquiry_items").css("border-bottom", "");
        });

    } else {
        $(".inquiry_content").not(content).stop(true, true).slideUp("fast").promise().done(function () {
            $(this).css("border-bottom", "").prev(".inquiry_items").css("border-bottom", "");

            $(this).children().css({
                visibility: "visible",
                position: "relative"
            });
        });

        content.stop(true, true).slideDown("fast", function () {
            content.css("border-bottom", "1px solid #333");
            content.prev(".inquiry_items").css("border-bottom", "1px solid #333");
        });
    }
}

// 상세페이지 문의 삭제
function deleteProductInquiry(productId, inquiryId, inquiryItem, inquiryContent) {
    let csrfToken = $("meta[name='_csrf']").attr("content");
    let csrfHeader = $("meta[name='_csrf_header']").attr("content");

    Swal.fire({
        text: "삭제하시겠습니까?",
        showCancelButton: true,
        cancelButtonText: '아니요',
        confirmButtonText: '예',
        customClass: mySwalConfirm,
        reverseButtons: true,
        buttonsStyling: false,
    }).then((result) => {
        if (result.isConfirmed) {
            $.ajax({
                type: 'DELETE',
                url: '/ajax/inquiries',
                async: false,
                data: {'inquiryId': inquiryId},
                beforeSend: function (xhr) {
                    xhr.setRequestHeader(csrfHeader, csrfToken)
                },
                success: function (data) {
                    if (data.status === 200) {
                        inquiryItem.remove(); // 질문 행 삭제
                        inquiryContent.remove(); // 내용 행 삭제
                        loadInquiries(productId, 1);
                    }
                },
                error: function () {
                    Swal.fire({
                        html: "삭제중 오류가 발생했습니다.<br>다시 시도해 주세요.",
                        showConfirmButton: true,
                        confirmButtonText: '확인',
                        customClass: mySwal,
                        buttonsStyling: false
                    });
                }
            })
        }
    });
}

// 상세페이지 문의 제출 전 유효성 검사
function validateBeforeInquiryModalSubmit() {
    let title = $('#inquiryModal #title').val().trim();
    let content = $('#inquiryModal #content').val().trim();

    if (title === '') {
        Swal.fire({
            text: "제목을 작성해 주세요.",
            showConfirmButton: true,
            confirmButtonText: '확인',
            customClass: mySwal,
            buttonsStyling: false
        });
        return false;
    }

    if (content === '') {
        Swal.fire({
            text: "내용을 작성해 주세요.",
            showConfirmButton: true,
            confirmButtonText: '확인',
            customClass: mySwal,
            buttonsStyling: false
        });
        return false;
    }

    $('#inquiryModal .submit_btn').prop('disabled', true);
    return true;
}