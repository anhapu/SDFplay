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
});

$('[data-toggle="popover"]').popover('hide');