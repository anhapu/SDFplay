var offset = 0;

$(document).ready(function() {
	$('.book').draggable({
		revert: "invalid",
		helper: "clone",
		start: function() {
			$(this).fadeOut(0);
		},
		stop: function() {
			$(this).fadeIn(0);
		}
	});

	$('#showcase').droppable({
		accept: "#wishlist > .book",
		drop: function(event, ui) {
			ui.draggable.removeClass("inList").fadeIn(1);
			$('#' + ui.draggable[0].id).appendTo('#showcase');
			$('#wishlist').remove('#' + ui.draggable[0].id);
		}
	});

	$('#wishlist').droppable({
		accept: "#showcase > .book",
		drop: function(event, ui) {
			ui.draggable.addClass("inList").fadeIn(1);
			$('#' + ui.draggable[0].id).appendTo('#wishlist');
			$('#showcase').remove('#' + ui.draggable[0].id);
		}
	});

	if ($('#wishlist-wrapper')[0] != undefined) {
		offset = $('#wishlist-wrapper').offset().top;
		affixLink();
		$(window).scroll(function() {affixCheck();});
	}
	
});

function affixCheck() {
	var leftHeight = 0;
	$('.tradecontent').each(function() {leftHeight += $(this).outerHeight();});
	if (leftHeight < $('#wishlist-wrapper').outerHeight()) {
		$('body').off("affix", "#wishlist-wrapper");
		$('#wishlist-wrapper').removeClass("affix").removeClass("affix-top").removeClass("affix-bottom").removeAttr("style");
		$('#wishlist-wrapper').attr("style", "top: " + offset + "px");
	}
	else {
		affixLink();
	}
}

function affixLink() {
	$('#wishlist-wrapper').removeAttr("style").affix({
		offset: {
			top: $('#wishlist-wrapper').offset().top, 
			bottom: 0
		}
	});
}

$('body').on('hidden.bs.modal', '#centralModal', function () {
    $(this).removeData('bs.modal');
});

$('[data-toggle="popover"]').popover('hide');
$('[data-toggle="tooltip"]').tooltip('hide');

