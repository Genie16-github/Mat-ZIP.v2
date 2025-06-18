package site.matzip.home;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import site.matzip.config.auth.PrincipalDetails;
import site.matzip.matzip.dto.MatzipRankDTO;
import site.matzip.matzip.service.MatzipService;
import site.matzip.member.dto.MemberRankDTO;
import site.matzip.member.service.MemberService;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {
    private final MatzipService matzipService;
    private final MemberService memberService;

    @GetMapping("/")
    public String showHome() {
        return "main/main";
    }

    @GetMapping("/main")
    public String showMain(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails != null) {
            model.addAttribute("memberId", principalDetails.getUserId());
        }

        return "matzip/create";
    }

    @GetMapping("/matzip")
    public String showMyMap(Model model, @AuthenticationPrincipal PrincipalDetails principalDetails) {
        if (principalDetails != null) {
            model.addAttribute("memberId", principalDetails.getUserId());
        }

        return "matzip/list";
    }

    @GetMapping("/ranking")
    public String showRanking(Model model) {
        List<MatzipRankDTO> matzipRankDTOS = matzipService.findAndConvertTopTenMatzip();
        List<MemberRankDTO> memberRankDtoList = memberService.findAndConvertTopTenMember();

        model.addAttribute("memberRankDtoList", memberRankDtoList);
        model.addAttribute("matzipRankDTOS", matzipRankDTOS);

        return "ranking/ranking";
    }
}
