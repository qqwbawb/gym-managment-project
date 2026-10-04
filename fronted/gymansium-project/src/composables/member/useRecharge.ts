import type { MemberType } from "@/api/member/MemberModel";
import { ref } from "vue";

export default function useRecharge() {

    const rechargeRef = ref<{ show: (row: MemberType) => void }>()

    //点击充值按钮
    const rechargeBtn = (row: MemberType) => {
        rechargeRef.value?.show(row)
    }

    return {
        rechargeBtn,
        rechargeRef
    }
}