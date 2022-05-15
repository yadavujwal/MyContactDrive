const toggleSidebar = () => {
    if ($('.sidebar').is(":visible"))
    {
        //true
        //band karna hai
        $(".sidebar").css("display", "none");
        $(".content").css("margin-left", "0%");
    } else {
        //false
        //show karna hai
        $(".sidebar").css("display", "block");
        $(".content").css("margin-left", "20%");
    }
};

const search = () => {
    let kw = $("#search-input").val();
    if (kw == "") {
        $(".search-result").hide();
    } else {
        let url = `http://localhost:9090/search/${kw}`;
        fetch(url).then((res) => {
            return res.json();
        }).then((data) => {
            //console.log(data);
            let text = `<div class='list-group'>`;
			data.forEach((contact)=>{
				text += `<a href='/user/${contact.cid}/contact' class= 'list-group-item list-group-item-action'>${contact.name} </a>`;
			})
			text += `</div>`;
            $(".search-result").html(text);
            $(".search-result").show();
        })
        

    }
}