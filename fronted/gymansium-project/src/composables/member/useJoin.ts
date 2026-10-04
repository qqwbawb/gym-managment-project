import { getCardListApi } from "@/api/member";
import type { MemberType } from "@/api/member/MemberModel";
import type { CardType } from "@/api/member_card/MemberModel";
import { onMounted, ref } from "vue";

export default function useJoin() {

    //会员卡列表数据
    const cardList = ref<CardType[]>([])
    //弹框属性
    const joinRef = ref<{ show: (row: MemberType) => void }>()

    //点击办卡按钮进行弹框
    const joinBtn = (row: MemberType) => {
        joinRef.value?.show(row)
    }
    //获取会员卡列表
    const getCardList = async () => {
        let res = await getCardListApi()
        if (res && res.code == 200) {
            cardList.value = res.data
        }
    }

    onMounted(() => {
        getCardList()
    })

    return {
        joinBtn,
        joinRef,
        cardList
    }
}